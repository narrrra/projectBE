package org.choongang.file.service;

import com.querydsl.core.BooleanBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.choongang.configs.FileProperties;
import org.choongang.file.entities.FileInfo;
import org.choongang.file.entities.QFileInfo;
import org.choongang.file.repositories.FileInfoRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.List;

import static org.springframework.data.domain.Sort.Order.desc;

@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(FileProperties.class)
public class FileInfoService {

    private final FileProperties props;
    private final HttpServletRequest request;
    private final FileInfoRepository repository;

    public FileInfo get(Long seq) {
        FileInfo item = repository.findById(seq).orElseThrow(FileNotFoundException::new);

        addFileInfo(item);

        return item;
    }

    /**
     *
     * @param gid
     * @param location
     * @param mode : ALL - 완료 + 미완료, DONE - 완료, UNDONE - 미완료
     * @return
     */
    public List<FileInfo> getList(String gid, String location, String mode) {
        mode = StringUtils.hasText(mode) ? mode.toUpperCase() : "ALL";

        QFileInfo fileInfo = QFileInfo.fileInfo;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(fileInfo.gid.eq(gid));

        if (StringUtils.hasText(location)) {
            builder.and(fileInfo.location.eq(location));
        }

        if (!mode.equals("ALL")) {
            builder.and(fileInfo.done.eq(mode.equals("DONE")));
        }

        List<FileInfo> items = (List<FileInfo>)repository.findAll(builder, Sort.by(desc("createdAt")));

        items.forEach(this::addFileInfo);

        return items;
    }

    public List<FileInfo> getList(String gid) {
        return getList(gid, null, "ALL");
    }

    public List<FileInfo> getList(String gid, String location) {
        return getList(gid, location, "ALL");
    }

    public List<FileInfo> getListDone(String gid, String location) {
        return getList(gid, location, "DONE");
    }

    public List<FileInfo> getListDone(String gid) {
        return getListDone(gid, null);
    }

    public void addFileInfo(FileInfo item) {
        long seq = item.getSeq();
        String extension = item.getExtension();
        String fileName = seq + (StringUtils.hasText(extension) ? "." + extension : "");
        String filePath = uploadDir(seq) + "/" + fileName;
        String fileUrl = uploadUrl(seq) + "/" + fileName;

        item.setFilePath(filePath);
        item.setFileUrl(fileUrl);

        String contentType = item.getContentType();
        if (contentType.indexOf("image/") != -1) { // 업로드한 파일이 이미지
            String thumbPath = thumbPath(seq) + "/main_" + fileName;
            String thumbUrl = thumbUrl(seq) + "/main_" + fileName;

            item.setThumbPath(thumbPath);
            item.setThumbUrl(thumbUrl);
        }
    }

    public String uploadDir(long seq) {
        String uploadDir = props.getPath() + (seq % 10L);
        File _uploadDir = new File(uploadDir);
        if (!_uploadDir.exists()) {
            _uploadDir.mkdir();
        }

        return uploadDir;
    }

    public String uploadUrl(long seq) {
        // http, https
        String host = String.format("%s://%s:%d",
                    request.getScheme(), request.getServerName(), request.getServerPort());

        return host + props.getUrl() + (seq % 10L);
    }

    public String thumbPath(long seq) {
        String dir = uploadDir(seq);

        String thumbDir= dir + "/thumbs";
        File _thumbDir = new File(thumbDir);
        if (!_thumbDir.exists()) {
            _thumbDir.mkdir();
        }

        return thumbDir;
    }

    public String thumbUrl(long seq) {
        return uploadUrl(seq) + "/thumbs";
    }
}
