package org.choongang.file.service;

import lombok.RequiredArgsConstructor;
import org.choongang.commons.exceptions.BadRequestException;
import org.choongang.file.controllers.RequestFileUpload;
import org.choongang.file.entities.FileInfo;
import org.choongang.file.repositories.FileInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FileInfoSaveService {

    private final FileInfoRepository fileInfoRepository;

    public List<FileInfo> save(RequestFileUpload form) {
        List<FileInfo> items = new ArrayList<>();
        MultipartFile[] files = form.getFile();

        if (files == null || files.length == 0) {
            throw new BadRequestException("Required.file.upload", true);
        }

        String gid = form.getGid();
        String location = form.getLocation();
        boolean imageOnly = Objects.requireNonNullElse(form.getImageOnly(), false);

        for (MultipartFile file : files) {
            // 파일명.확장자, 파일명.파일명.확장자 - lastIndexOf
            String fileName = file.getOriginalFilename();
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
            String contentType = file.getContentType(); // image/jpeg, image/png ...

            // 이미지만 업로드일때 이미지가 아닌 파일은 건너뛰기
            if (imageOnly && contentType.indexOf("image/") == -1) {
                continue;
            }

            FileInfo item = FileInfo.builder()
                    .gid(gid)
                    .location(location)
                    .fileName(fileName)
                    .contentType(contentType)
                    .extension(extension)
                    .file(file)
                    .build();

            items.add(item);
        }


        fileInfoRepository.saveAllAndFlush(items);

        return items;
    }
}
