package com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.blocks;

import com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.block.WALBlockLog;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.entry.IByte;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.bytes.BytesMessage;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
public class WALBlocksLog implements IByte<WALBlocksLog> {

    private WALHeader header;

    private List<WALBlockLog> entryList;

    /**
     * tailer 为空时， 可能文件没写完就宕机了，也可能是正在写的新文件
     */
    private WALTrailer trailer;

    public WALBlocksLog(WALHeader header, List<WALBlockLog> entryList, WALTrailer trailer) {
        this.header = header;
        this.entryList = entryList;
        this.trailer = trailer;
    }

    /**
     * IByte bean 转 bytes
     */
    @Override
    public byte[] toBytes() {
        return new byte[0];
    }

    /**
     * bytes 转 IByte bean
     */
    public static final WALBlocksLog fromBytes(byte[] bytes) {
        BytesMessage mm = new BytesMessage(bytes);

        return fromBytes(mm);
    }

    /**
     * bytes 转 IByte bean
     */
    public static final WALBlocksLog fromBytes(BytesMessage mm) {
        WALHeader walHeader = WALHeader.fromBytes(mm);
        WALTrailer trailer = null;

        List<WALBlockLog> entryList = new ArrayList<>();
        while (mm.hasRemaining()){
            WALBlockLog blockLog = WALBlockLog.fromBytes(mm);

            entryList.add(blockLog);

            try{
                // 查看是否为结束标记。 是的话，尝试读取 trailer
                int position = mm.position();
                if(mm.hasRemaining()){
                    byte flag = mm.read();
                    mm.position(position);

                    if(flag == WALTrailer.NULL_MARK){
                        trailer = WALTrailer.fromBytes(mm);

                        break;
                    }
                }

                // do nothing
            }catch (Exception ex){
                // ignore
                ex.printStackTrace();
            }
        }

        return new WALBlocksLog(walHeader, entryList, trailer);
    }
}
