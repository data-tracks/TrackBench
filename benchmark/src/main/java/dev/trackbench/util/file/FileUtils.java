package dev.trackbench.util.file;

import dev.trackbench.display.BlockingMessage;
import dev.trackbench.display.Display;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    public static final ExecutorService executor = Executors.newFixedThreadPool( NUM_THREADS );


    public static File createFolderAndMove( File folder, String name ) {
        List<File> files = List.of( Objects.requireNonNull( folder.listFiles() ) );

        File newFolder = createFolder( folder, name );

        try {
            for ( File file : files ) {
                if ( file.isFile() ) {
                    Files.move( file.toPath(), new File( newFolder, file.getName() ).toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING );
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
        BlockingMessage msg = new BlockingMessage( String.format( Display.indent() + "Counting %s", target ) );
        Display.INSTANCE.next( msg );
        try ( AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open( target.toPath(), StandardOpenOption.READ ) ) {
            long fileSize = fileChannel.size();
            if ( fileSize <= 0 ) {
                return 0;
            }

            long chunkSize = fileSize / NUM_THREADS;

            if ( chunkSize * NUM_THREADS < fileSize ) {
                // we correct this
                chunkSize++;
            }

            List<Future<Long>> futures = new ArrayList<>();

            for ( int i = 0; i < NUM_THREADS; i++ ) {
                long startPos = i * chunkSize;
                long endPos = Math.min( fileSize, startPos + chunkSize );
                futures.add( FileUtils.executor.submit( () -> countLinesInChunk( fileChannel, startPos, endPos ) ) );
            }

            long totalLineCount = 0;
            for ( Future<Long> future : futures ) {
                totalLineCount += future.get();
            }

            if ( debug ) {
                Display.INSTANCE.info( "File {} has {} lines", target.getName(), totalLineCount );
            }
            return totalLineCount + 1; // we did not find a \n but als the file was not empty to one line
        } catch ( ExecutionException | InterruptedException | IOException e ) {
            throw new RuntimeException( e );
        } finally {
            msg.finish();
        }
    }


    private static long countLinesInChunk( AsynchronousFileChannel fileChannel, long position, long endPos ) {
        long lineCount = 0;
        long chunkSize = endPos - position;
        if ( chunkSize <= 0 ) {
            return lineCount;
        }
        ByteBuffer buffer = ByteBuffer.allocate( Math.min( (int) chunkSize, BUFFER_SIZE ) );
        long bytesReadTotal = 0;

        while ( bytesReadTotal < chunkSize ) {
            Future<Integer> future = fileChannel.read( buffer, position + bytesReadTotal );
            try {
                int bytesRead = future.get();
                if ( bytesRead == -1 ) {
                    return lineCount; // End of file
                }

                bytesReadTotal += bytesRead;
                buffer.flip();
                byte[] data = new byte[bytesRead];
                buffer.get( data );

                if ( bytesReadTotal > chunkSize ) {
                    // we have read too much
                    bytesRead -= (int) (bytesReadTotal - chunkSize);
                }

                for ( int i = 0; i < bytesRead; i++ ) {
                    if ( data[i] == '\n' ) {
                        lineCount++;
                    }
                }
                buffer.clear();

                if ( bytesReadTotal >= chunkSize ) {
                    return lineCount;
                }

            } catch ( Exception e ) {
                throw new RuntimeException( e );
            }
        }
        return lineCount;
    }


    @NotNull
    public static File getJson( File path, String name ) {
        return new File( path, "%s.json".formatted( name.replace( ".json", "" ) ) );
    }


    public static boolean hasJsonFile( File path, String name ) {
        return new File( path, "%s.json".formatted( name ) ).exists();
    }


    public static void copy( File source, File target ) {
        try ( BufferedInputStream inputStream = new BufferedInputStream( new FileInputStream( source ) );
                BufferedOutputStream outputStream = new BufferedOutputStream( new FileOutputStream( target, true ) ) ) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ( (bytesRead = inputStream.read( buffer )) != -1 ) {
                outputStream.write( buffer, 0, bytesRead );
            }

        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }


    public static void close() {
        executor.shutdown();
    }


    public static void deleteFolder( File folder ) {
        if ( !folder.exists() ) {
            return;
        }
        deleteFolderOrFile( folder );
    }


    private static void deleteFolderOrFile( File file ) {
        if ( file.isFile() ) {
            boolean success = file.delete();
            if ( !success ) {
                throw new RuntimeException( "Error deleting file: " + file.getAbsolutePath() );
            }
            return;
        }
        for ( File f : Objects.requireNonNull( file.listFiles() ) ) {
            deleteFolderOrFile( f );
        }
        boolean success = file.delete();
        if ( !success ) {
            throw new RuntimeException( "Error deleting file: " + file.getAbsolutePath() );
        }

    }


    public static void deleteFile( File file ) {
        if ( file.isFile() ) {
            boolean success = file.delete();
        } else if ( !file.exists() ) {
            return;
        } else {
            throw new RuntimeException( "Error deleting file: " + file.getAbsolutePath() );
        }
    }

}
