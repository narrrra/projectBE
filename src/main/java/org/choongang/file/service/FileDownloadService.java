package org.choongang.file.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.choongang.file.entities.FileInfo;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
@RequiredArgsConstructor
public class FileDownloadService {
    private final HttpServletResponse response;
    private final FileInfoService infoService;

    public void download(Long seq) {
        FileInfo item = infoService.get(seq);
        String filePath = item.getFilePath();
        String fileName = item.getFileName();
        try {
            fileName = new String(fileName.getBytes(), "ISO8859_1");
        } catch (UnsupportedEncodingException e) {}

        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        // ServletOutputStream, PrintWriter

        try (FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis)) {
            OutputStream out = response.getOutputStream();

            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            response.setHeader("Content-Type", "application/octet-stream");
            response.setIntHeader("Expires", 0);
            response.setHeader("Cache-Control", "must-revalidate");
            response.setHeader("Pragma", "public");
            response.setHeader("Content-Length", String.valueOf(file.length()));

            while(bis.available() > 0) {
                out.write(bis.read());
            }

            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
