package edu.sjsu.cmpe272.simpleblog.server.repository;

import edu.sjsu.cmpe272.simpleblog.server.entity.Follow;
import edu.sjsu.cmpe272.simpleblog.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Follow findFollowByFollowerAndFollowee(String follower, String followee);
    List<Follow> findFollowsByFollowerOrderByFolloweeAsc(String follower);

}
