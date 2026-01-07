package com.hancom.weboffice.controller;

import com.hancom.weboffice.hwp.filter.WebhwpUnix;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/japi")
public class RestApiController {
    private static final Logger LOG = LoggerFactory.getLogger(RestApiController.class);
    private WebhwpUnix webhwpUnix = null;

    private DocumentConverter converter;

    RestApiController() {

        try {
            this.webhwpUnix = new WebhwpUnix();
            OfficeManager officeManager = LocalOfficeManager.builder()
                    .portNumbers(2002)
                    .install()
                    .build();
            officeManager.start();
            this.converter = LocalConverter.builder().officeManager(officeManager).build();
        } catch (OfficeException e) {
            LOG.error(e.getMessage());
        }

    }

    @Value("${tmp-folder}")
    private String tmpPath;

    @RequestMapping("/")
    public String main() {
        return "ready to start";
    }

    @PostMapping(value = "/hwp/convert-to-pdf")
    public ResponseEntity<byte[]> hwpToPdf(@RequestParam("file") MultipartFile file) {
        File inputFile = null;
        File outputFile = null;
        try {
            //String originName = file.getOriginalFilename().split("\\.")[0];
            if(file == null || file.isEmpty()){
                return ResponseEntity.internalServerError().body(null);
            }

            // 입력 파일 저장 (Hancom SDK는 파일 경로 필요)
            inputFile = new File(tmpPath + "/" + UUID.randomUUID());
            file.transferTo(inputFile);

            // 출력 파일명 생성
            String tempFileName = UUID.randomUUID() + ".pdf";
            outputFile = new File(tmpPath + File.separator + tempFileName);

            // 변환 실행
            webhwpUnix.saveFromHwp("HWP", inputFile.getAbsolutePath(),
                tmpPath, tempFileName, "PDF", "", webhwpUnix.HncPath, "");

            // PDF 파일을 byte[]로 읽기
            byte[] pdfBytes = Files.readAllBytes(outputFile.toPath());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            LOG.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        } finally {
            // 임시 파일 정리 (입력 + 출력 모두)
            if (inputFile != null && inputFile.exists()) {
                inputFile.delete();
            }
            if (outputFile != null && outputFile.exists()) {
                outputFile.delete();
            }
        }
    }

    @PostMapping(value = "/hwpx/convert-to-pdf")
    public ResponseEntity<byte[]> hwpxToPdf(@RequestParam("file") MultipartFile file) {
        File inputFile = null;
        File outputFile = null;
        try {
            //String originName = file.getOriginalFilename().split("\\.")[0];
            if(file == null || file.isEmpty()){
                return ResponseEntity.internalServerError().body(null);
            }

            // 입력 파일 저장 (Hancom SDK는 파일 경로 필요)
            inputFile = new File(tmpPath + "/" + UUID.randomUUID());
            file.transferTo(inputFile);

            // 출력 파일명 생성
            String tempFileName = UUID.randomUUID() + ".pdf";
            outputFile = new File(tmpPath + File.separator + tempFileName);

            // 변환 실행
            webhwpUnix.saveFromHwp("HWPX", inputFile.getAbsolutePath(),
                tmpPath, tempFileName, "PDF", "", webhwpUnix.HncPath, "");

            // PDF 파일을 byte[]로 읽기
            byte[] pdfBytes = Files.readAllBytes(outputFile.toPath());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            LOG.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        } finally {
            // 임시 파일 정리 (입력 + 출력 모두)
            if (inputFile != null && inputFile.exists()) {
                inputFile.delete();
            }
            if (outputFile != null && outputFile.exists()) {
                outputFile.delete();
            }
        }
    }

    @PostMapping(value = "/all/convert-to-pdf")
    public ResponseEntity<byte[]> docToPdf(@RequestParam("file") MultipartFile file) throws IOException {
        if(file == null || file.isEmpty()){
            return ResponseEntity.internalServerError().body(null);
        }
        String originName = file.getOriginalFilename().split("\\.")[0];
        LOG.debug("originName : " + originName);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            DocumentFormat targetFormat = DefaultDocumentFormatRegistry.getFormatByExtension("pdf");
            LOG.debug(targetFormat.toString());
            converter.convert(file.getInputStream()).to(baos).as(targetFormat).execute();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF).body(baos.toByteArray());

        } catch (IOException | OfficeException e) {
            LOG.error(e.getMessage());
        }
        return ResponseEntity.internalServerError().body(null);

    }
}
