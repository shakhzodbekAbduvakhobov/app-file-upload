package uz.pdp.appfileread.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.appfileread.entity.AttachmentContent;

import java.util.Optional;

public interface AttachmentContentRepository extends JpaRepository<AttachmentContent, Integer> {

    Optional<AttachmentContent> findByAttachmentId(Integer attachment_id);
}
