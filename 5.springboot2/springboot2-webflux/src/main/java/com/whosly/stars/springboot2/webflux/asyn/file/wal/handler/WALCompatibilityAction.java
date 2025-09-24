package com.whosly.stars.springboot2.webflux.asyn.file.wal.handler;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.ToString;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;

@ToString
public class WALCompatibilityAction {

    public static final String OS_NAME = System.getProperty("os.name");

    public static final boolean LINUX = OS_NAME.startsWith("Linux");

    public static final boolean MAC_OS_X = OS_NAME.startsWith("Mac OS X");


    @Getter
    private final Long databaseId;
    @Getter
    private final Long tableId;
    @Getter
    private final String fileName;
    @Getter
    private Date createDate;

    @Getter
    private FileHandlerModel model;

    /**
     *
     * @param databaseId
     * @param tableId
     * @param fileName
     * @param checkAction 是否做初始化检查。   true 为检查
     * @throws IOException
     */
    public WALCompatibilityAction(Long databaseId, Long tableId, String fileName, boolean checkAction)
            throws IOException {
        this.databaseId = databaseId;
        this.tableId = tableId;

        if(checkAction){
            FileHandlerModel model = fileInit(fileName);
            this.fileName = model.getFinalFileName();
            this.model = model;
        }else{
            this.fileName = fileName;
            this.model = FileHandlerModel.builder()
                    .openAndCreate(false)
                    .openSize(getFile(fileName).length())
                    .finalFileName(fileName)
                    .build();;
        }
    }

    /**
     * @return  key:  是否为刚创建的文件
     * @throws IOException
     */
    private FileHandlerModel fileInit(String targetFileName) throws IOException {
        boolean openAndCreate = tryCreateNewFile(targetFileName);

        return FileHandlerModel.builder()
                .openAndCreate(openAndCreate)
                .openSize(getFile(targetFileName).length())
                .finalFileName(targetFileName)
                .build();
    }

    /**
     * create new file
     *
     * @return 是否为刚创建的文件， false  文件未新建
     */
    public boolean tryCreateNewFile(String targetFileName) throws IOException {
        File newFile = getFile(targetFileName);

        if(!newFile.exists()){
            newFile.getParentFile().mkdirs();
            newFile.createNewFile();
            fsync(newFile.toPath(), false);

            return true;
        }

        return false;
    }

    private File getFile(String targetFileName){
        String pathName = getFilePath(getDatabaseId(), getTableId(), targetFileName);

        return new File(pathName);
    }

    /**
     * 文件所处的目录路径
     */
    public static final String getDirectoryAbsolutePath(Long databaseId, Long tableId){
        StringBuffer sb = new StringBuffer("storage/cstore/wal");
        sb.append(File.separator);
        sb.append(databaseId);
        sb.append(File.separator);
        sb.append(tableId);

        return sb.toString();
    }

    /**
     * 文件路径
     */
    public static final String getFilePath(WALCompatibilityAction compatibilityAction){
        return getFilePath(compatibilityAction.getDatabaseId(), compatibilityAction.getTableId(),
                compatibilityAction.getFileName());
    }

    /**
     * 文件路径
     */
    public static final String getFilePath(Long databaseId, Long tableId, String tableName){
        return getDirectoryAbsolutePath(databaseId, tableId) + File.separator + tableName;
    }

    /**
     * 文件对象
     */
    public static final File getFile(Long databaseId, Long tableId, String tableName){
        String filePath = getFilePath(databaseId, tableId, tableName);

        return new File(filePath);
    }

    /**
     * CURRENT 文件路径
     */
    public static final String getCurrentFilePath(Long databaseId, Long tableId){
        return getDirectoryAbsolutePath(databaseId, tableId) + File.separator + "CURRENT";
    }

    /**
     * Ensure that any writes to the given file is written to the storage device that contains it.
     * @param fileToSync the file to fsync
     * @param isDir if true, the given file is a directory (we open for read and ignore IOExceptions,
     *  because not all file systems and operating systems allow to fsync on a directory)
     */
    private static final void fsync(Path fileToSync, boolean isDir) throws IOException {
        // If the file is a directory we have to open read-only, for regular files we must open r/w for the fsync to have an effect.
        // See http://blog.httrack.com/blog/2013/11/15/everything-you-always-wanted-to-know-about-fsync/
        try (final FileChannel file = FileChannel.open(fileToSync, isDir ? StandardOpenOption.READ : StandardOpenOption.WRITE)) {
            file.force(true);
        } catch (IOException ioe) {
            if (isDir) {

                Preconditions.checkArgument(!(LINUX || MAC_OS_X),
                        "On Linux and MacOSX fsyncing a directory should not throw " +
                                "IOException, we just don't want to rely " +
                                "on that in production (undocumented). Got: " + ioe);

                // Ignore exception if it is a directory
                return;
            }
            // Throw original exception
            throw ioe;
        }
    }

    public static final WALCompatibilityAction of(FileSchmaData fileSchmaData)
            throws IOException {
        return of(fileSchmaData.getDatabaseId(), fileSchmaData.getTableId(),
                fileSchmaData.getSourceFileName());
    }

    public static final WALCompatibilityAction of(Long databaseId, Long tableId, String fileName)
            throws IOException {
        return of(databaseId, tableId, fileName, true);
    }

    public static final WALCompatibilityAction of(Long databaseId, Long tableId, String fileName, boolean checkAction)
            throws IOException {
        return new WALCompatibilityAction(databaseId, tableId, fileName, checkAction);
    }

}
