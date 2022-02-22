package study.datajpa.repository;

import study.datajpa.entity.Member;

import java.util.List;
//내가 기능을 직접구현하고싶을 때
public interface MemberRepositoryCustom {

    List<Member> findMemberCustom();
}
