package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Replaces one local employee file atomically, after writing and verifying a protected sibling temporary file.
 * Supports a single writer; concurrent writers and live external editing are outside this contract.
 */
class AtomicFileWriter {
    static final String ATOMIC_MOVE_UNSUPPORTED = "Atomic replacement is not supported for this data file.";
    static final String PROTECTION_UNSUPPORTED = "Cannot preserve the required file access protections.";

    private static final Set<PosixFilePermission> OWNER_READ_WRITE =
            Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);

    private final FileOperations files;

    AtomicFileWriter() {
        this(new FileOperations());
    }

    AtomicFileWriter(FileOperations files) {
        this.files = requireNonNull(files);
    }

    /** Writes a new file, refusing to replace any existing file or symbolic link. */
    void writeNew(Path destination, byte[] contents) throws IOException {
        write(destination, contents, true);
    }

    /**
     * Writes all contents before atomically replacing the resolved destination. Replacement is the commit point.
     */
    void write(Path destination, byte[] contents) throws IOException {
        write(destination, contents, false);
    }

    private void write(Path destination, byte[] contents, boolean shouldCreateNew) throws IOException {
        requireNonNull(destination);
        requireNonNull(contents);
        Path absolute = destination.toAbsolutePath();
        Path target = shouldCreateNew
                ? resolve(absolute.getParent()).resolve(absolute.getFileName()) : resolve(absolute);
        if (shouldCreateNew && Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
            throw new FileAlreadyExistsException(target.toString());
        }
        Protection protection = inspect(target);
        Files.createDirectories(target.getParent());
        Path parent = target.getParent().toRealPath();
        target = parent.resolve(target.getFileName());
        Path temporary = null;
        boolean hasReservedTarget = false;
        try {
            temporary = files.createTemporary(parent, protection.creationAttributes());
            protection.restrict(temporary);
            try (WritableFile output = files.open(temporary)) {
                ByteBuffer buffer = ByteBuffer.wrap(contents);
                while (buffer.hasRemaining()) {
                    output.write(buffer);
                }
                output.force();
            }
            protection.prepare(temporary);
            if (shouldCreateNew) {
                // CREATE_NEW is the collision check even if another backup appeared after inspection.
                files.createNew(target, protection.creationAttributes());
                hasReservedTarget = true;
            }
            try {
                files.replace(temporary, target);
            } catch (AtomicMoveNotSupportedException e) {
                throw new IOException(ATOMIC_MOVE_UNSUPPORTED, e);
            }
        } catch (IOException | RuntimeException failure) {
            if (temporary != null) {
                try {
                    files.delete(temporary);
                } catch (IOException | RuntimeException cleanupFailure) {
                    failure.addSuppressed(cleanupFailure);
                }
            }
            if (hasReservedTarget) {
                try {
                    files.delete(target);
                } catch (IOException | RuntimeException cleanupFailure) {
                    failure.addSuppressed(cleanupFailure);
                }
            }
            throw failure;
        }
        // No fallible work after successful replacement: the saved file is now committed.
    }

    /** Resolves existing links, including parents, without mistaking a dangling link for a missing file. */
    private Path resolve(Path path) throws IOException {
        try {
            return path.toRealPath();
        } catch (NoSuchFileException e) {
            if (Files.isSymbolicLink(path) || path.getParent() == null) {
                throw e;
            }
            return resolve(path.getParent()).resolve(path.getFileName());
        }
    }

    private Protection inspect(Path target) throws IOException {
        boolean exists = true;
        Path probe = target;
        try {
            if (!Files.readAttributes(target, BasicFileAttributes.class).isRegularFile()) {
                throw new IOException("The data file destination is not a regular file: " + target);
            }
        } catch (NoSuchFileException e) {
            exists = false;
            probe = target.getParent();
            while (true) {
                try {
                    Files.readAttributes(probe, BasicFileAttributes.class);
                    break;
                } catch (NoSuchFileException missingParent) {
                    probe = probe.getParent();
                }
            }
        }
        try {
            PosixFileAttributeView posix = files.view(probe, PosixFileAttributeView.class);
            Protection protection;
            if (posix != null) {
                PosixFileAttributes attributes = posix.readAttributes();
                if (exists && !attributes.permissions().contains(PosixFilePermission.OWNER_WRITE)) {
                    throw new AccessDeniedException(target.toString());
                }
                protection = new PosixProtection(exists ? attributes : null);
            } else {
                AclFileAttributeView acl = requiredView(probe, AclFileAttributeView.class);
                DosFileAttributeView dos = requiredView(probe, DosFileAttributeView.class);
                if (exists && dos.readAttributes().isReadOnly()) {
                    throw new AccessDeniedException(target.toString());
                }
                protection = new WindowsProtection(exists ? acl.getOwner() : null,
                        exists ? List.copyOf(acl.getAcl()) : null);
            }
            if (exists && !files.isWritable(target)) {
                throw new AccessDeniedException(target.toString());
            }
            return protection;
        } catch (IOException | UnsupportedOperationException e) {
            throw protectionFailure(e);
        }
    }

    private <V extends FileAttributeView> V requiredView(Path path, Class<V> type) throws IOException {
        V view = files.view(path, type);
        if (view == null) {
            throw new IOException(PROTECTION_UNSUPPORTED);
        }
        return view;
    }

    private static IOException protectionFailure(Exception cause) {
        if (cause instanceof AccessDeniedException denied) {
            return denied;
        }
        return new IOException(PROTECTION_UNSUPPORTED, cause);
    }

    /** Captures only the supported access protections; it never changes ownership. */
    private abstract class Protection {
        abstract FileAttribute<?>[] creationAttributes();

        final void restrict(Path temporary) throws IOException {
            try {
                restrictAccess(temporary);
            } catch (IOException | UnsupportedOperationException e) {
                throw protectionFailure(e);
            }
        }

        final void prepare(Path temporary) throws IOException {
            try {
                prepareAccess(temporary);
            } catch (IOException | UnsupportedOperationException e) {
                throw protectionFailure(e);
            }
        }

        abstract void restrictAccess(Path temporary) throws IOException;

        abstract void prepareAccess(Path temporary) throws IOException;
    }

    /** Preserves the nine POSIX permission bits, with the naturally assigned owner and group. */
    private class PosixProtection extends Protection {
        private final PosixFileAttributes original;
        private PosixFileAttributes created;

        PosixProtection(PosixFileAttributes original) {
            this.original = original;
        }

        @Override
        FileAttribute<?>[] creationAttributes() {
            return new FileAttribute<?>[] {PosixFilePermissions.asFileAttribute(OWNER_READ_WRITE)};
        }

        @Override
        void restrictAccess(Path temporary) throws IOException {
            created = requiredView(temporary, PosixFileAttributeView.class).readAttributes();
            requireProtection(created.permissions().equals(OWNER_READ_WRITE));
            if (original != null) {
                requireProtection(sameOwnership(original, created));
            }
        }

        @Override
        void prepareAccess(Path temporary) throws IOException {
            PosixFileAttributeView view = requiredView(temporary, PosixFileAttributeView.class);
            Set<PosixFilePermission> permissions = original == null ? OWNER_READ_WRITE : original.permissions();
            view.setPermissions(permissions);
            PosixFileAttributes prepared = view.readAttributes();
            requireProtection(prepared.permissions().equals(permissions) && sameOwnership(created, prepared));
        }

        private boolean sameOwnership(PosixFileAttributes first, PosixFileAttributes second) {
            return first.owner().equals(second.owner()) && first.group().equals(second.group());
        }
    }

    /** Copies same-provider access ACL entries as an ordered, opaque list. */
    private class WindowsProtection extends Protection {
        private final UserPrincipal originalOwner;
        private final List<AclEntry> originalAcl;
        private UserPrincipal createdOwner;
        private List<AclEntry> restrictedAcl;

        WindowsProtection(UserPrincipal originalOwner, List<AclEntry> originalAcl) {
            this.originalOwner = originalOwner;
            this.originalAcl = originalAcl;
        }

        @Override
        FileAttribute<?>[] creationAttributes() {
            return new FileAttribute<?>[0];
        }

        @Override
        void restrictAccess(Path temporary) throws IOException {
            AclFileAttributeView view = requiredView(temporary, AclFileAttributeView.class);
            createdOwner = view.getOwner();
            restrictedAcl = List.of(AclEntry.newBuilder().setType(AclEntryType.ALLOW)
                    .setPrincipal(createdOwner).setPermissions(EnumSet.allOf(AclEntryPermission.class)).build());
            view.setAcl(restrictedAcl);
            verify(temporary, view, restrictedAcl);
            requireProtection(originalOwner == null || originalOwner.equals(createdOwner));
        }

        @Override
        void prepareAccess(Path temporary) throws IOException {
            AclFileAttributeView view = requiredView(temporary, AclFileAttributeView.class);
            List<AclEntry> acl = originalAcl == null ? restrictedAcl : originalAcl;
            view.setAcl(acl);
            verify(temporary, view, acl);
        }

        private void verify(Path temporary, AclFileAttributeView view, List<AclEntry> expected) throws IOException {
            requireProtection(view.getOwner().equals(createdOwner) && view.getAcl().equals(expected)
                    && !requiredView(temporary, DosFileAttributeView.class).readAttributes().isReadOnly());
        }
    }

    private static void requireProtection(boolean condition) throws IOException {
        if (!condition) {
            throw new IOException(PROTECTION_UNSUPPORTED);
        }
    }

    /** Provides the small file-operation seam used to exercise failures around the commit point. */
    static class FileOperations {
        void createNew(Path destination, FileAttribute<?>... attributes) throws IOException {
            Files.createFile(destination, attributes);
        }

        Path createTemporary(Path parent, FileAttribute<?>... attributes) throws IOException {
            try {
                return Files.createTempFile(parent, ".huntr-", ".tmp", attributes);
            } catch (UnsupportedOperationException e) {
                throw protectionFailure(e);
            }
        }

        <V extends FileAttributeView> V view(Path path, Class<V> type) throws IOException {
            return Files.getFileAttributeView(path, type, LinkOption.NOFOLLOW_LINKS);
        }

        boolean isWritable(Path path) {
            return Files.isWritable(path);
        }

        WritableFile open(Path temporary) throws IOException {
            FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE);
            return new WritableFile() {
                @Override
                public int write(ByteBuffer buffer) throws IOException {
                    return channel.write(buffer);
                }

                @Override
                public void force() throws IOException {
                    channel.force(true);
                }

                @Override
                public void close() throws IOException {
                    channel.close();
                }
            };
        }

        void replace(Path temporary, Path destination) throws IOException {
            Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        }

        void delete(Path temporary) throws IOException {
            Files.deleteIfExists(temporary);
        }
    }

    /** Writes, forces and closes the temporary file before any replacement can occur. */
    interface WritableFile extends AutoCloseable {
        int write(ByteBuffer buffer) throws IOException;

        void force() throws IOException;

        @Override
        void close() throws IOException;
    }
}
