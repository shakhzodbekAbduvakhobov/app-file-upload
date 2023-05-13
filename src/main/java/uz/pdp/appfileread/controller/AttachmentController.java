package uz.pdp.appfileread.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import uz.pdp.appfileread.entity.Attachment;
import uz.pdp.appfileread.entity.AttachmentContent;
import uz.pdp.appfileread.repository.AttachmentContentRepository;
import uz.pdp.appfileread.repository.AttachmentRepository;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/attachment")
public class AttachmentController {

    private static final String uploadFile = "server";

    @Autowired
    AttachmentRepository attachmentRepository;

    @Autowired
    AttachmentContentRepository attachmentContentRepository;

    @PostMapping
    public String uploadDB(MultipartHttpServletRequest request) throws IOException {
        Iterator<String> fileNames = request.getFileNames();
        MultipartFile multipartFile = request.getFile(fileNames.next());

        if (!multipartFile.isEmpty()) {
            Attachment attachment = new Attachment();
            attachment.setFileName(multipartFile.getName());
            attachment.setContentType(multipartFile.getContentType());
            attachment.setSize(attachment.getSize());
            Attachment savedAttachment = attachmentRepository.save(attachment);

            AttachmentContent attachmentContent = new AttachmentContent();
            attachmentContent.setContent(multipartFile.getBytes());
            attachmentContent.setAttachment(savedAttachment);
            attachmentContentRepository.save(attachmentContent);
            return "Saved";
        }
        return "Error";
    }

    @PostMapping("/file")
    public String uploadFileSystem(MultipartHttpServletRequest request) throws IOException {
        Iterator<String> fileNames = request.getFileNames();
        MultipartFile file = request.getFile(fileNames.next());
        if (file != null) {
            Attachment attachment = new Attachment();
            String originalFilename = file.getOriginalFilename();
            attachment.setFileName(originalFilename);
            attachment.setSize(file.getSize());
            attachment.setContentType(file.getContentType());

            String[] split = originalFilename.split("\\.");
            String name = UUID.randomUUID().toString() + "." + split[split.length - 1];
            attachment.setName(name);
            attachmentRepository.save(attachment);

            Path path = Paths.get(uploadFile + "/" + name);
            Files.copy(file.getInputStream(), path);
            return "Saqlandi";
        }
        return "Saqlanmadi";
    }

    @GetMapping("/{id}")
    public void getFileDB(@PathVariable Integer id, HttpServletResponse response) throws IOException {
        Optional<Attachment> optionalAttachment = attachmentRepository.findById(id);
        if (optionalAttachment.isPresent()) {
            Attachment attachment = optionalAttachment.get();
            Optional<AttachmentContent> optionalAttachmentContent = attachmentContentRepository.findByAttachmentId(id);
            if (optionalAttachmentContent.isPresent()) {
                AttachmentContent attachmentContent = optionalAttachmentContent.get();
                response.setHeader("Content-Disposition", "attachment; fileName=\"" + attachment.getFileName() + "\"");
                response.setContentType(attachment.getContentType());
                FileCopyUtils.copy(attachmentContent.getContent(), response.getOutputStream());

            }
        }
    }

    @GetMapping("/file/{id}")
    public void getFileSystem(@PathVariable Integer id, HttpServletResponse response) throws IOException {
        Optional<Attachment> optionalAttachment = attachmentRepository.findById(id);
        if (optionalAttachment.isPresent()){
            Attachment attachment = optionalAttachment.get();
            response.setHeader("Content-Disposition", "attachment; fileName=\"" + attachment.getFileName() + "\"");
            response.setContentType(attachment.getContentType());

            FileInputStream fileInputStream = new FileInputStream(uploadFile + "/" + attachment.getName());
            FileCopyUtils.copy(fileInputStream, response.getOutputStream());
        }
    }
}
