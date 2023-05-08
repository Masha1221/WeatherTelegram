package com.example.battle_bot.models;

import lombok.*;
import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "posts")
public class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String description;
    private long chatId;
    private String form;
    @OneToOne
    @JoinColumn(name = "image_id")
    private ImageEntity image;
}
