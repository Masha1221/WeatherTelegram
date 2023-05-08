package com.example.weathertelegram.models;

import lombok.*;
import lombok.experimental.Accessors;


import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "images")
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String telegramFileId;
    @OneToOne
    @JoinColumn(name = "binary_content_id")
    private BinaryContentEntity binaryContentEntity;
    private int fileSize;
}
