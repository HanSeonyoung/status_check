package check.demo.model.read;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cctv")
@Getter @Setter
public class Cctv {
    @Id
    private Long id;

//    @Column(name = "ip_address", nullable = false)
    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

}
