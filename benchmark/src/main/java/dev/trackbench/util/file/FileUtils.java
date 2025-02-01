package dev.trackbench.util.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtils {

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
        try ( RandomAccessFile file = new RandomAccessFile( target, "r" ); FileChannel channel = file.getChannel() ) {

            MappedByteBuffer buffer = channel.map( FileChannel.MapMode.READ_ONLY, 0, channel.size() );
            int lineCount = 0;

            for ( int i = 0; i < buffer.limit(); i++ ) {
                if ( buffer.get( i ) == '\n' ) {
                    lineCount++;
                }
            }

            if ( debug ) {
                log.info( "File {} has {} lines", target.getName(), lineCount );
            }

            return lineCount;
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

}
