package dev.trackbench.util.file;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dev.trackbench.display.Display;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

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
                Display.INSTANCE.info( "File {} has {} lines", target.getName(), lineCount );
            }

            return lineCount;
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
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
