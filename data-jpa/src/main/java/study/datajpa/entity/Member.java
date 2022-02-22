package study.datajpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id","username","team"})
public class Member extends BaseEntity{

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String username;

    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    public Member(String username) {
        this.username = username;
    }

    public Member(String username, int i, Team team) {
        this.username = username;
        this.age = i;
        if(team!=null){
            changeTeam(team);
        }
    }

    public Member(String username, int age) {
        this(username, age, null);
    }


    public void changeTeam(Team team){
        this.team = team;
        team.getMembers().add(this);
    }
}
