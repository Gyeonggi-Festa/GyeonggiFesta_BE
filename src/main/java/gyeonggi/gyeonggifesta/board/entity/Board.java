package gyeonggi.gyeonggifesta.board.entity;

import gyeonggi.gyeonggifesta.event.entity.Event;
import gyeonggi.gyeonggifesta.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long id;

    @Setter
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", unique = true)
    @Setter
    private Event event;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @Builder
    public Board(String name, Event event) {
        this.name = name;
        this.event = event;
    }

    public int getPostsSize() {
        return posts.size();
    }

    public void addPost(Post post) {
        if (!posts.contains(post)) {
            posts.add(post);
        }
        post.setBoard(this);
    }

    public void removePost(Post post) {
        posts.remove(post);
        post.setBoard(null);
    }
}
