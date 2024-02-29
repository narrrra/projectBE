package org.choongang.file.service;

import lombok.RequiredArgsConstructor;
import org.choongang.commons.exceptions.UnAuthorizedException;
import org.choongang.file.entities.FileInfo;
import org.choongang.file.repositories.FileInfoRepository;
import org.choongang.member.MemberUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileDeleteService {

    private final FileInfoService infoService;
    private final MemberUtil memberUtil;
    private final FileInfoRepository repository;

    public void delete(Long seq) {
        FileInfo item = infoService.get(seq);
        String createdBy = item.getCreatedBy();
        if (!memberUtil.isAdmin() && StringUtils.hasText(createdBy)
                && (!memberUtil.isLogin() || !createdBy.equals(memberUtil.getMember().getEmail()))) {
            throw new UnAuthorizedException();
        }

        String filePath = item.getFilePath();
        String thumbPath = item.getThumbPath();

        File _filePath = new File(filePath);
        if (_filePath.exists()) _filePath.delete();

        if (StringUtils.hasText(thumbPath)) {
            File _thumbPath = new File(thumbPath);
            if (_thumbPath.exists()) _thumbPath.delete();
        }

        repository.delete(item);
        repository.flush();
    }

    public void delete(String gid, String location) {
        List<FileInfo> items = infoService.getList(gid, location);
        items.forEach(item -> delete(item.getSeq()));
    }

    public void delete(String gid) {
        delete(gid, null);
    }
}