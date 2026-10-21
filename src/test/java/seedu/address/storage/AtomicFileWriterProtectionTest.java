package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AtomicFileWriterProtectionTest {
    private static final byte[] ORIGINAL = "original".getBytes(StandardCharsets.UTF_8);
    private static final byte[] UPDATED = "updated".getBytes(StandardCharsets.UTF_8);
    private static final UserPrincipal OWNER = () -> "creator";
    private static final UserPrincipal OTHER_OWNER = () -> "other owner";
    private static final GroupPrincipal OTHER_GROUP = () -> "other group";

    @TempDir
    public Path folder;

    @Test
    public void write_posixOwnershipMismatch_rejectsWithoutChangingOwnership() throws Exception {
        assumePosix();
        for (String mismatch : List.of("owner", "group")) {
            Path destination = Files.write(folder.resolve(mismatch + ".json"), ORIGINAL);
            ProtectionFiles files = new ProtectionFiles() {
                @Override
                <V extends FileAttributeView> V view(Path path, Class<V> type) throws IOException {
                    V view = super.view(path, type);
                    if (type == PosixFileAttributeView.class && !path.equals(temporary)) {
                        return type.cast(posixView((PosixFileAttributeView) view, mismatch, false));
                    }
                    return view;
                }
            };
            assertProtectionFailure(files, destination, true);
            assertFalse(files.opened);
        }
    }

    @Test
    public void write_posixRestrictionOrFinalRoundTripFails_rejectsBeforeReplacement() throws Exception {
        assumePosix();
        for (String mismatch : List.of("permissions", "owner", "ignoredPermissions")) {
            for (boolean existing : List.of(false, true)) {
                if (!existing && mismatch.equals("ignoredPermissions")) {
                    continue; // A new file already has the required 0600; no permission change is needed.
                }
                Path destination = folder.resolve(mismatch + existing + ".json");
                if (existing) {
                    Files.write(destination, ORIGINAL);
                    Files.setPosixFilePermissions(destination, PosixFilePermissions.fromString("rw-r-----"));
                }
                ProtectionFiles files = new ProtectionFiles() {
                    @Override
                    <V extends FileAttributeView> V view(Path path, Class<V> type) throws IOException {
                        V view = super.view(path, type);
                        if (type == PosixFileAttributeView.class && path.equals(temporary)) {
                            String change = mismatch.equals("owner") && !opened ? "unchanged" : mismatch;
                            return type.cast(posixView((PosixFileAttributeView) view, change, true));
                        }
                        return view;
                    }
                };
                assertProtectionFailure(files, destination, existing);
                if (mismatch.equals("permissions")) {
                    assertFalse(files.opened);
                }
            }
        }
    }

    @Test
    public void write_windowsPolicy_restrictsThenCopiesOrderedOpaqueAcl() throws Exception {
        for (boolean existing : List.of(false, true)) {
            Path destination = folder.resolve("acl" + existing + ".json");
            if (existing) {
                Files.write(destination, ORIGINAL);
            }
            WindowsFiles files = new WindowsFiles();
            new AtomicFileWriter(files).write(destination, UPDATED);
            assertTrue(files.opened);
            assertArrayEquals(UPDATED, Files.readAllBytes(destination));
            assertEquals(existing ? files.destinationAcl : List.of(ownerEntry()), files.temporaryAcl);
            assertEquals(List.of(List.of(ownerEntry()), existing ? files.destinationAcl : List.of(ownerEntry())),
                    files.aclWrites);
            assertEquals(1, files.replaceCalls);
        }
    }

    @Test
    public void write_windowsAclCannotRoundTrip_failsForNewAndExistingDestination() throws Exception {
        for (boolean existing : List.of(false, true)) {
            for (int failedWrite : List.of(1, 2)) {
                Path destination = folder.resolve("acl-failure" + existing + failedWrite + ".json");
                if (existing) {
                    Files.write(destination, ORIGINAL);
                }
                WindowsFiles files = new WindowsFiles();
                files.failedAclWrite = failedWrite;
                assertProtectionFailure(files, destination, existing);
                assertEquals(failedWrite == 2, files.opened);
            }
        }
    }

    @Test
    public void write_windowsOwnershipMismatch_rejectsBeforeWriting() throws Exception {
        Path destination = Files.write(folder.resolve("owner.json"), ORIGINAL);
        WindowsFiles files = new WindowsFiles();
        files.destinationOwner = OTHER_OWNER;
        assertProtectionFailure(files, destination, true);
        assertFalse(files.opened);
    }

    @Test
    public void write_windowsRequiredViewMissing_rejectsForNewAndExistingDestination() throws Exception {
        for (boolean existing : List.of(false, true)) {
            for (Class<?> missing : List.of(AclFileAttributeView.class, DosFileAttributeView.class)) {
                Path destination = folder.resolve("missing" + existing + missing.getSimpleName() + ".json");
                if (existing) {
                    Files.write(destination, ORIGINAL);
                }
                WindowsFiles files = new WindowsFiles();
                files.missingView = missing;
                assertProtectionFailure(files, destination, existing);
                assertFalse(files.opened);
            }
        }
    }

    @Test
    public void write_windowsAclAccessDenied_retainsClassification() throws Exception {
        Path destination = Files.write(folder.resolve("denied.json"), ORIGINAL);
        WindowsFiles files = new WindowsFiles();
        files.aclFailure = new AccessDeniedException("temporary", "destination", "cannot set ACL");
        IOException failure = assertThrows(IOException.class, () ->
                new AtomicFileWriter(files).write(destination, UPDATED));
        assertSame(files.aclFailure, failure);
        assertArrayEquals(ORIGINAL, Files.readAllBytes(destination));
        assertFalse(files.opened);
        assertEquals(0, files.replaceCalls);
    }

    @Test
    public void write_windowsTemporaryReadOnly_rejectsWithoutClearingProtection() throws Exception {
        Path destination = folder.resolve("absent.json");
        WindowsFiles files = new WindowsFiles();
        files.temporaryReadOnly = true;
        assertProtectionFailure(files, destination, false);
        assertFalse(files.opened);
    }

    private void assertProtectionFailure(ProtectionFiles files, Path destination, boolean existing) throws Exception {
        IOException failure = assertThrows(IOException.class, () ->
                new AtomicFileWriter(files).write(destination, UPDATED));
        assertEquals(AtomicFileWriter.PROTECTION_UNSUPPORTED, failure.getMessage());
        assertEquals(0, files.replaceCalls);
        if (existing) {
            assertArrayEquals(ORIGINAL, Files.readAllBytes(destination));
        } else {
            assertTrue(Files.notExists(destination));
        }
        assertTrue(files.temporary == null || Files.notExists(files.temporary));
    }

    private void assumePosix() {
        assumeTrue(Files.getFileAttributeView(folder, PosixFileAttributeView.class) != null,
                "Requires POSIX attributes for native permission round-trip checks");
    }

    private static PosixFileAttributeView posixView(PosixFileAttributeView delegate, String mismatch,
            boolean temporary) {
        return (PosixFileAttributeView) Proxy.newProxyInstance(PosixFileAttributeView.class.getClassLoader(),
                new Class<?>[] {PosixFileAttributeView.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                    case "readAttributes":
                        PosixFileAttributes original = delegate.readAttributes();
                        return Proxy.newProxyInstance(PosixFileAttributes.class.getClassLoader(),
                                new Class<?>[] {PosixFileAttributes.class}, (unused, attribute, parameters) -> {
                                    if (attribute.getName().equals(mismatch)) {
                                        return switch (mismatch) {
                                        case "owner" -> OTHER_OWNER;
                                        case "group" -> OTHER_GROUP;
                                        default -> PosixFilePermissions.fromString("rw-rw-rw-");
                                        };
                                    }
                                    return attribute.invoke(original, parameters);
                                });
                    case "setPermissions":
                        assertTrue(temporary);
                        if (mismatch.equals("ignoredPermissions")) {
                            return null;
                        }
                        return method.invoke(delegate, args);
                    case "setOwner":
                    case "setGroup":
                        throw new AssertionError("The writer must never change ownership");
                    default:
                        return method.invoke(delegate, args);
                    }
                });
    }

    private static AclEntry ownerEntry() {
        return AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(OWNER)
                .setPermissions(EnumSet.allOf(AclEntryPermission.class)).build();
    }

    private static class ProtectionFiles extends AtomicFileWriter.FileOperations {
        protected Path temporary;
        protected boolean opened;
        protected int replaceCalls;

        @Override
        Path createTemporary(Path parent, FileAttribute<?>... attributes) throws IOException {
            temporary = super.createTemporary(parent, attributes);
            return temporary;
        }

        @Override
        AtomicFileWriter.WritableFile open(Path path) throws IOException {
            opened = true;
            return super.open(path);
        }

        @Override
        void replace(Path temporary, Path destination) throws IOException {
            replaceCalls++;
            super.replace(temporary, destination);
        }
    }

    private static class WindowsFiles extends ProtectionFiles {
        private final List<AclEntry> destinationAcl = List.of(
                AclEntry.newBuilder().setType(AclEntryType.DENY).setPrincipal(OTHER_OWNER)
                        .setPermissions(AclEntryPermission.WRITE_DATA).build(),
                ownerEntry(),
                AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(OTHER_OWNER)
                        .setPermissions(AclEntryPermission.READ_DATA).setFlags(AclEntryFlag.INHERIT_ONLY).build());
        private final List<List<AclEntry>> aclWrites = new ArrayList<>();
        private List<AclEntry> temporaryAcl = List.of();
        private UserPrincipal destinationOwner = OWNER;
        private int failedAclWrite;
        private Class<?> missingView;
        private IOException aclFailure;
        private boolean temporaryReadOnly;

        @Override
        <V extends FileAttributeView> V view(Path path, Class<V> type) {
            if (type == PosixFileAttributeView.class || type == missingView) {
                return null;
            }
            boolean isTemporary = path.equals(temporary);
            if (type == AclFileAttributeView.class) {
                return type.cast(new AclFileAttributeView() {
                    @Override
                    public String name() {
                        return "acl";
                    }

                    @Override
                    public List<AclEntry> getAcl() {
                        return isTemporary ? temporaryAcl : destinationAcl;
                    }

                    @Override
                    public void setAcl(List<AclEntry> acl) throws IOException {
                        assertTrue(isTemporary);
                        if (aclFailure != null) {
                            throw aclFailure;
                        }
                        aclWrites.add(List.copyOf(acl));
                        temporaryAcl = aclWrites.size() == failedAclWrite ? List.of() : List.copyOf(acl);
                    }

                    @Override
                    public UserPrincipal getOwner() {
                        return isTemporary ? OWNER : destinationOwner;
                    }

                    @Override
                    public void setOwner(UserPrincipal owner) {
                        throw new AssertionError("The writer must never change ownership");
                    }
                });
            }
            return type.cast(Proxy.newProxyInstance(DosFileAttributeView.class.getClassLoader(),
                    new Class<?>[] {DosFileAttributeView.class}, (proxy, method, args) -> {
                        assertEquals("readAttributes", method.getName(), "DOS protection must never be cleared");
                        BasicFileAttributes basic = Files.readAttributes(path, BasicFileAttributes.class);
                        return Proxy.newProxyInstance(DosFileAttributes.class.getClassLoader(),
                                new Class<?>[] {DosFileAttributes.class}, (unused, attribute, parameters) -> {
                                    return switch (attribute.getName()) {
                                    case "isReadOnly" -> isTemporary && temporaryReadOnly;
                                    case "isHidden", "isSystem", "isArchive" -> false;
                                    default -> BasicFileAttributes.class.getMethod(attribute.getName()).invoke(basic);
                                    };
                                });
                    }));
        }

        @Override
        AtomicFileWriter.WritableFile open(Path path) throws IOException {
            assertEquals(List.of(ownerEntry()), temporaryAcl, "Owner-only ACL must precede the first byte");
            return super.open(path);
        }
    }
}
