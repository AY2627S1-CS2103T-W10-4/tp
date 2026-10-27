package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AtomicFileWriterTest {
    private static final byte[] ORIGINAL = "last stored employee data".getBytes(StandardCharsets.UTF_8);
    private static final byte[] UPDATED = "complete updated employee data 雇员".getBytes(StandardCharsets.UTF_8);
    private static final Set<PosixFilePermission> PRIVATE_PERMISSIONS = PosixFilePermissions.fromString("rw-------");

    @TempDir
    public Path folder;

    @Test
    public void write_newAndExistingFile_replacesCompleteContents() throws Exception {
        Path destination = folder.resolve("new/parents/employees.json");
        AtomicFileWriter writer = new AtomicFileWriter();
        writer.write(destination, ORIGINAL);
        assertArrayEquals(ORIGINAL, Files.readAllBytes(destination));
        writer.write(destination, UPDATED);
        assertArrayEquals(UPDATED, Files.readAllBytes(destination));
        assertOnlyDestination(destination);
    }

    @Test
    public void writeNew_existingSnapshot_preservesOriginalBytes() throws Exception {
        Path destination = folder.resolve("backup.json");
        AtomicFileWriter writer = new AtomicFileWriter();
        writer.writeNew(destination, ORIGINAL);
        assertRestricted(destination);

        assertThrows(FileAlreadyExistsException.class, () -> writer.writeNew(destination, UPDATED));

        assertArrayEquals(ORIGINAL, Files.readAllBytes(destination));
        assertOnlyDestination(destination);
    }

    @Test
    public void writeNew_destinationAppearsBeforeReservation_preservesOtherSnapshot() throws Exception {
        Path destination = folder.resolve("backup.json");
        AtomicFileWriter.FileOperations files = new AtomicFileWriter.FileOperations() {
            @Override
            void createNew(Path target, FileAttribute<?>... attributes) throws IOException {
                Files.write(target, ORIGINAL);
                super.createNew(target, attributes);
            }
        };

        assertThrows(FileAlreadyExistsException.class, () ->
                new AtomicFileWriter(files).writeNew(destination, UPDATED));

        assertArrayEquals(ORIGINAL, Files.readAllBytes(destination));
        assertOnlyDestination(destination);
    }

    @Test
    public void writeNew_eachPreReplacementFault_removesTemporaryAndReservation() throws Exception {
        for (Fault fault : Fault.values()) {
            if (fault == Fault.NONE) {
                continue;
            }
            Path destination = newDestination("newBackup" + fault.name(), false);
            FaultyFiles files = new FaultyFiles(fault);

            assertThrows(IOException.class, () -> new AtomicFileWriter(files).writeNew(destination, UPDATED));

            assertTrue(Files.notExists(destination));
            assertTrue(files.temporary == null || Files.notExists(files.temporary));
        }
    }

    @Test
    public void writeNew_existingLeafLinks_preservesLinksAndTargets() throws Exception {
        for (boolean existingTarget : List.of(false, true)) {
            Path target = newDestination("linkTarget" + existingTarget, existingTarget);
            Path link = createLink(folder.resolve("backup" + existingTarget + ".json"), target);

            assertThrows(FileAlreadyExistsException.class, () ->
                    new AtomicFileWriter().writeNew(link, UPDATED));

            assertTrue(Files.isSymbolicLink(link));
            assertEquals(target, Files.readSymbolicLink(link));
            assertDestination(target, existingTarget);
        }
    }

    @Test
    public void write_eachPreReplacementFault_preservesExistingOrAbsentDestination() throws Exception {
        for (Fault fault : Fault.values()) {
            if (fault == Fault.NONE) {
                continue;
            }
            for (boolean existing : List.of(false, true)) {
                Path destination = newDestination(fault.name() + existing, existing);
                FaultyFiles files = new FaultyFiles(fault);
                IOException thrown = assertThrows(IOException.class, () ->
                        new AtomicFileWriter(files).write(destination, UPDATED));
                if (fault == Fault.ATOMIC_UNSUPPORTED) {
                    assertEquals(AtomicFileWriter.ATOMIC_MOVE_UNSUPPORTED, thrown.getMessage());
                    assertSame(files.failure, thrown.getCause());
                    assertEquals(1, files.replaceCalls);
                } else if (fault == Fault.RESTRICT || fault == Fault.PREPARE) {
                    assertEquals(AtomicFileWriter.PROTECTION_UNSUPPORTED, thrown.getMessage());
                    assertSame(files.failure, thrown.getCause());
                } else {
                    assertSame(files.failure, thrown);
                }
                if (fault != Fault.REPLACE && fault != Fault.ATOMIC_UNSUPPORTED) {
                    assertEquals(0, files.replaceCalls);
                }
                assertDestination(destination, existing);
                assertTrue(files.temporary == null || Files.notExists(files.temporary));
                if (fault == Fault.WRITE) {
                    assertTrue(files.bytesWritten > 0 && files.bytesWritten < UPDATED.length);
                }
            }
        }
    }

    @Test
    public void write_shortWrites_consumesEntireBufferAndClosesBeforeReplacement() throws Exception {
        Path destination = folder.resolve("employees.json");
        FaultyFiles files = new FaultyFiles(Fault.NONE);
        new AtomicFileWriter(files).write(destination, UPDATED);
        assertArrayEquals(UPDATED, Files.readAllBytes(destination));
        assertTrue(files.writeCalls > 1);
        assertTrue(files.forced);
        assertTrue(files.closed);
        assertEquals(1, files.replaceCalls);
        assertEquals(0, files.deleteCalls);
    }

    @Test
    public void write_cleanupAlsoFails_retainsOriginalFailureAndDestination() throws Exception {
        for (boolean existing : List.of(false, true)) {
            Path destination = newDestination("cleanup" + existing, existing);
            FaultyFiles files = new FaultyFiles(Fault.WRITE);
            files.cleanupFailure = new IOException("cleanup failed");
            IOException thrown = assertThrows(IOException.class, () ->
                    new AtomicFileWriter(files).write(destination, UPDATED));
            assertSame(files.failure, thrown);
            assertArrayEquals(new Throwable[] {files.cleanupFailure}, thrown.getSuppressed());
            assertDestination(destination, existing);
            assertTrue(Files.exists(files.temporary));
            assertEquals(0, files.replaceCalls);
        }
    }

    @Test
    public void write_writeAndCloseFail_keepsWriteFailureWithSuppressedCloseFailure() throws Exception {
        FaultyFiles files = new FaultyFiles(Fault.WRITE);
        files.closeFailure = new IOException("close also failed");
        IOException thrown = assertThrows(IOException.class, () ->
                new AtomicFileWriter(files).write(folder.resolve("absent.json"), UPDATED));
        assertSame(files.failure, thrown);
        assertArrayEquals(new Throwable[] {files.closeFailure}, thrown.getSuppressed());
        assertEquals(0, files.replaceCalls);
        assertTrue(Files.notExists(files.temporary));
    }

    @Test
    public void write_success_doesNotAttemptCleanupAfterCommit() throws Exception {
        FaultyFiles files = new FaultyFiles(Fault.NONE);
        files.cleanupFailure = new IOException("must never run after replacement");
        Path destination = newDestination("committed", true);
        new AtomicFileWriter(files).write(destination, UPDATED);
        assertArrayEquals(UPDATED, Files.readAllBytes(destination));
        assertEquals(0, files.deleteCalls);
    }

    @Test
    public void write_newFile_restrictsAccessBeforeFirstByteAndRetainsProtection() throws Exception {
        Path destination = folder.resolve("private.json");
        FaultyFiles files = new FaultyFiles(Fault.NONE);
        new AtomicFileWriter(files).write(destination, UPDATED);
        assertTrue(files.checkedProtectionBeforeWrite);
        assertRestricted(destination);
    }

    @Test
    public void write_posixExistingFile_preservesPermissionsOwnerAndGroup() throws Exception {
        assumePosix();
        Path destination = newDestination("posix", true);
        Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxr-x---");
        Files.setPosixFilePermissions(destination, permissions);
        PosixFileAttributes original = Files.readAttributes(destination, PosixFileAttributes.class);
        new AtomicFileWriter().write(destination, UPDATED);
        PosixFileAttributes updated = Files.readAttributes(destination, PosixFileAttributes.class);
        assertEquals(permissions, updated.permissions());
        assertEquals(original.owner(), updated.owner());
        assertEquals(original.group(), updated.group());
        assertArrayEquals(UPDATED, Files.readAllBytes(destination));
    }

    @Test
    public void write_posixWithoutOwnerWrite_rejectsWithoutChangingProtection() throws Exception {
        assumePosix();
        Path destination = newDestination("read-only", true);
        Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("r--rw----");
        Files.setPosixFilePermissions(destination, permissions);
        try {
            assertThrows(AccessDeniedException.class, () -> new AtomicFileWriter().write(destination, UPDATED));
            assertDestination(destination, true);
            assertEquals(permissions, Files.getPosixFilePermissions(destination));
            assertOnlyDestination(destination);
        } finally {
            Files.setPosixFilePermissions(destination, PRIVATE_PERMISSIONS);
        }
    }

    @Test
    public void write_windowsExistingFile_preservesOrderedAclAndOwner() throws Exception {
        assumeWindowsAcl();
        Path destination = newDestination("windows", true);
        AclFileAttributeView view = Files.getFileAttributeView(destination, AclFileAttributeView.class);
        List<AclEntry> originalAcl = List.copyOf(view.getAcl());
        var owner = view.getOwner();
        new AtomicFileWriter().write(destination, UPDATED);
        assertEquals(originalAcl, view.getAcl());
        assertEquals(owner, view.getOwner());
        assertArrayEquals(UPDATED, Files.readAllBytes(destination));
    }

    @Test
    public void write_windowsReadOnly_rejectsWithoutClearingFlag() throws Exception {
        assumeWindowsAcl();
        Path destination = newDestination("dos-read-only", true);
        DosFileAttributeView view = Files.getFileAttributeView(destination, DosFileAttributeView.class);
        view.setReadOnly(true);
        try {
            assertThrows(AccessDeniedException.class, () -> new AtomicFileWriter().write(destination, UPDATED));
            assertTrue(view.readAttributes().isReadOnly());
            assertDestination(destination, true);
            assertOnlyDestination(destination);
        } finally {
            view.setReadOnly(false);
        }
    }

    @Test
    public void write_unwritableExistingFile_rejectsBeforeCreatingTemporaryFile() throws Exception {
        Path destination = newDestination("unwritable", true);
        FaultyFiles files = new FaultyFiles(Fault.NONE) {
            @Override
            boolean isWritable(Path path) {
                return false;
            }
        };
        assertThrows(AccessDeniedException.class, () -> new AtomicFileWriter(files).write(destination, UPDATED));
        assertEquals(null, files.temporary);
        assertDestination(destination, true);
    }

    @Test
    public void write_missingAttributeViews_failsBeforeReplacement() throws Exception {
        for (boolean existing : List.of(false, true)) {
            Path destination = newDestination("views" + existing, existing);
            FaultyFiles files = new FaultyFiles(Fault.NONE) {
                @Override
                <V extends FileAttributeView> V view(Path path, Class<V> type) {
                    return null;
                }
            };
            IOException thrown = assertThrows(IOException.class, () ->
                    new AtomicFileWriter(files).write(destination, UPDATED));
            assertEquals(AtomicFileWriter.PROTECTION_UNSUPPORTED, thrown.getMessage());
            assertEquals(0, files.replaceCalls);
            assertEquals(null, files.temporary);
            assertDestination(destination, existing);
        }
    }

    @Test
    public void write_bareRelativeFilename_usesWorkingDirectory() throws Exception {
        Path destination = Path.of("atomic-writer-test-" + UUID.randomUUID() + ".json");
        try {
            new AtomicFileWriter().write(destination, UPDATED);
            assertArrayEquals(UPDATED, Files.readAllBytes(destination.toAbsolutePath()));
        } finally {
            Files.deleteIfExists(destination);
        }
    }

    @Test
    public void write_symbolicLinkParent_createsTemporaryAlongsideRealDestination() throws Exception {
        Path realParent = Files.createDirectory(folder.resolve("real"));
        Path link = createLink(folder.resolve("linked"), realParent);
        FaultyFiles files = new FaultyFiles(Fault.NONE);
        new AtomicFileWriter(files).write(link.resolve("missing/employees.json"), UPDATED);
        Path destination = realParent.resolve("missing/employees.json");
        assertArrayEquals(UPDATED, Files.readAllBytes(destination));
        assertEquals(destination.getParent().toRealPath(), files.temporary.getParent());
        assertTrue(Files.isSymbolicLink(link));
    }

    @Test
    public void write_validLeafLink_replacesTargetAndRetainsLink() throws Exception {
        Path target = newDestination("target", true);
        Path link = createLink(folder.resolve("employees.json"), folder.relativize(target));
        new AtomicFileWriter().write(link, UPDATED);
        assertTrue(Files.isSymbolicLink(link));
        assertArrayEquals(UPDATED, Files.readAllBytes(target));
        assertArrayEquals(UPDATED, Files.readAllBytes(link));
    }

    @Test
    public void write_danglingLeafLink_rejectsAndRetainsLink() throws Exception {
        Path target = folder.resolve("missing.json");
        Path link = createLink(folder.resolve("employees.json"), target);
        assertThrows(IOException.class, () -> new AtomicFileWriter().write(link, UPDATED));
        assertTrue(Files.isSymbolicLink(link));
        assertEquals(target, Files.readSymbolicLink(link));
        assertTrue(Files.notExists(target));
    }

    @Test
    public void write_directoryDestination_rejectsWithoutChangingContents() throws Exception {
        Path directory = Files.createDirectory(folder.resolve("directory"));
        Path sentinel = directory.resolve("keep.json");
        Files.write(sentinel, ORIGINAL);
        assertThrows(IOException.class, () -> new AtomicFileWriter().write(directory, UPDATED));
        assertTrue(Files.isDirectory(directory));
        assertArrayEquals(ORIGINAL, Files.readAllBytes(sentinel));
        assertOnlyDestination(sentinel);
    }

    private Path newDestination(String name, boolean existing) throws IOException {
        Path destination = Files.createDirectory(folder.resolve(name)).resolve("employees.json");
        if (existing) {
            Files.write(destination, ORIGINAL);
        }
        return destination;
    }

    private void assertDestination(Path destination, boolean existing) throws IOException {
        if (existing) {
            assertArrayEquals(ORIGINAL, Files.readAllBytes(destination));
        } else {
            assertTrue(Files.notExists(destination, LinkOption.NOFOLLOW_LINKS));
        }
    }

    private void assertOnlyDestination(Path destination) throws IOException {
        try (var siblings = Files.list(destination.getParent())) {
            assertEquals(List.of(destination), siblings.toList());
        }
    }

    private void assumePosix() {
        assumeTrue(Files.getFileAttributeView(folder, PosixFileAttributeView.class) != null,
                "Requires POSIX permission attributes");
    }

    private void assumeWindowsAcl() {
        assumeTrue(Files.getFileAttributeView(folder, PosixFileAttributeView.class) == null
                && Files.getFileAttributeView(folder, AclFileAttributeView.class) != null
                && Files.getFileAttributeView(folder, DosFileAttributeView.class) != null,
                "Requires Windows access ACL and DOS views");
    }

    private Path createLink(Path link, Path target) throws IOException {
        try {
            return Files.createSymbolicLink(link, target);
        } catch (UnsupportedOperationException | java.nio.file.FileSystemException e) {
            assumeTrue(false, "Symbolic-link creation is unavailable: " + e);
            throw e;
        }
    }

    private static void assertRestricted(Path path) throws IOException {
        PosixFileAttributeView posix = Files.getFileAttributeView(path, PosixFileAttributeView.class);
        if (posix != null) {
            assertEquals(PRIVATE_PERMISSIONS, posix.readAttributes().permissions());
        } else {
            AclFileAttributeView acl = Files.getFileAttributeView(path, AclFileAttributeView.class);
            AclEntry ownerAccess = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(acl.getOwner())
                    .setPermissions(EnumSet.allOf(AclEntryPermission.class)).build();
            assertEquals(List.of(ownerAccess), acl.getAcl());
            assertFalse(Files.getFileAttributeView(path, DosFileAttributeView.class).readAttributes().isReadOnly());
        }
    }

    private enum Fault {
        NONE, CREATE, RESTRICT, OPEN, WRITE, FORCE, CLOSE, PREPARE, REPLACE, ATOMIC_UNSUPPORTED, ACCESS_DENIED
    }

    private static class FaultyFiles extends AtomicFileWriter.FileOperations {
        private final Fault fault;
        private final IOException failure;
        private IOException cleanupFailure;
        private IOException closeFailure;
        private Path temporary;
        private int bytesWritten;
        private int writeCalls;
        private int replaceCalls;
        private int deleteCalls;
        private boolean forced;
        private boolean closed;
        private boolean checkedProtectionBeforeWrite;

        FaultyFiles(Fault fault) {
            this.fault = fault;
            failure = switch (fault) {
                case ATOMIC_UNSUPPORTED -> new AtomicMoveNotSupportedException("temporary", "target", "unsupported");
                case ACCESS_DENIED -> new AccessDeniedException("temporary", "target", "diagnostic text");
                default -> new IOException("injected " + fault);
            };
        }

        @Override
        Path createTemporary(Path parent, FileAttribute<?>... attributes) throws IOException {
            failAt(Fault.CREATE);
            temporary = super.createTemporary(parent, attributes);
            return temporary;
        }

        @Override
        <V extends FileAttributeView> V view(Path path, Class<V> type) throws IOException {
            if (path.equals(temporary)) {
                failAt(Fault.ACCESS_DENIED);
                failAt(closed ? Fault.PREPARE : Fault.RESTRICT);
            }
            return super.view(path, type);
        }

        @Override
        AtomicFileWriter.WritableFile open(Path path) throws IOException {
            failAt(Fault.OPEN);
            AtomicFileWriter.WritableFile delegate = super.open(path);
            return new AtomicFileWriter.WritableFile() {
                @Override
                public int write(ByteBuffer buffer) throws IOException {
                    if (writeCalls == 0) {
                        assertRestricted(path);
                        checkedProtectionBeforeWrite = true;
                    } else {
                        failAt(Fault.WRITE);
                    }
                    writeCalls++;
                    int originalLimit = buffer.limit();
                    buffer.limit(Math.min(buffer.position() + 3, originalLimit));
                    int written = delegate.write(buffer);
                    buffer.limit(originalLimit);
                    bytesWritten += written;
                    return written;
                }

                @Override
                public void force() throws IOException {
                    failAt(Fault.FORCE);
                    delegate.force();
                    forced = true;
                }

                @Override
                public void close() throws IOException {
                    delegate.close();
                    closed = true;
                    failAt(Fault.CLOSE);
                    if (closeFailure != null) {
                        throw closeFailure;
                    }
                }
            };
        }

        @Override
        void replace(Path source, Path target) throws IOException {
            replaceCalls++;
            assertTrue(forced && closed);
            failAt(Fault.REPLACE);
            failAt(Fault.ATOMIC_UNSUPPORTED);
            super.replace(source, target);
        }

        @Override
        void delete(Path path) throws IOException {
            deleteCalls++;
            if (cleanupFailure != null) {
                throw cleanupFailure;
            }
            super.delete(path);
        }

        private void failAt(Fault stage) throws IOException {
            if (fault == stage) {
                throw failure;
            }
        }
    }
}
