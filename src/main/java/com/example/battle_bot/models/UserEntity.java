package com.example.battle_bot.models;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private long chatId;
    private boolean isBonusCodeGivenTelegram;
    private boolean isBonusCodeGivenInstagram;
    private Date dateOfGivenBonusCodeInstagram;
    private Date dateOfGivenBonusCodeTelegram;
}
