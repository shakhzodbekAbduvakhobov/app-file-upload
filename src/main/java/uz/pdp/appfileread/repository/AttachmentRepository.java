package uz.pdp.appfileread.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.appfileread.entity.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {
}
