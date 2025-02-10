package dev.trackbench.util.file;

import dev.trackbench.display.BlockingMessage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dev.trackbench.display.Display;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class FileUtils {
    private static final int BUFFER_SIZE = 4 * 1024 * 1024;
    public static final int NUM_THREADS = 64;
    public static final ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

    public static File createFolderAndMove( File folder, String name ) {
        List<File> files = List.of( Objects.requireNonNull( folder.listFiles() ) );

        File newFolder = createFolder( folder, name );

        try {
            for ( File file : files ) {
                if ( file.isFile() ) {
                    Files.move( file.toPath(), new File( newFolder, file.getName() ).toPath() , StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING );
                }
            }
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
        return newFolder;
    }

    public static File createFolder( File folder, String name ) {
        File newFolder = new File( folder, name );
        if ( !newFolder.exists() ) {
            newFolder.mkdirs();
        }
        return newFolder;
    }


    public static List<File> getJsonFiles( File folder ) {
        List<File> files = new ArrayList<>();
        for ( File file : Objects.requireNonNull( folder.listFiles() ) ) {
            if ( file.isFile() && file.getName().endsWith( ".json" ) ) {
                files.add( file );
            }
        }
        return files;
    }

    public static long countLines( File target, boolean debug ) {
        BlockingMessage msg = new BlockingMessage( String.format( "Counting %s", target ) );
        Display.INSTANCE.next( msg );
        try ( AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(target.toPath(), StandardOpenOption.READ)) {
            long fileSize = fileChannel.size();
            long chunkSize = fileSize / NUM_THREADS;
            List<Future<Long>> futures = new ArrayList<>();

            for (int i = 0; i < NUM_THREADS; i++) {
                long startPos = i * chunkSize;
                long endPos = (i == NUM_THREADS - 1) ? fileSize : startPos + chunkSize;
                futures.add(FileUtils.executor.submit(() -> countLinesInChunk(fileChannel, startPos, endPos)));
            }

            long totalLineCount = 0;
            for (Future<Long> future : futures) {
                totalLineCount += future.get();
            }

            if ( debug ) {
                Display.INSTANCE.info( "File {} has {} lines", target.getName(), totalLineCount );
            }
            return totalLineCount;
        } catch ( ExecutionException | InterruptedException | IOException e ) {
            throw new RuntimeException( e );
        }finally {
            msg.finish();
        }
    }

    private static long countLinesInChunk(AsynchronousFileChannel fileChannel, long start, long end) {
        long lineCount = 0;
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        try {
            while (start < end) {
                long remaining = end - start;
                int readSize = (int) Math.min(BUFFER_SIZE, remaining);
                buffer.clear();

                Future<Integer> readFuture = fileChannel.read(buffer, start);
                int bytesRead = readFuture.get();
                if (bytesRead == -1) break;

                buffer.flip();
                for (int i = 0; i < bytesRead; i++) {
                    if (buffer.get() == '\n') {
                        lineCount++;
                    }
                }
                start += bytesRead;
            }
        } catch (Exception e) {
            throw new RuntimeException( e );
        }

        return lineCount;
    }

    @NotNull
    public static File getJson(File path, String name) {
        return new File( path, "%s.json".formatted( name.replace( ".json", "" ) ) );
    }

    public static boolean hasJsonFile(File path, String name) {
        return new File( path, "%s.json".formatted(name) ).exists();
    }

    public static void copy(File source, File target) {
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(source));
             BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(target, true))) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

        } catch (IOException e) {
            throw new RuntimeException( e );
        }
    }
}
