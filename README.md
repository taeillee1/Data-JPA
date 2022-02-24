@NoArgsConstructor : 파라미터가 없는 기본 생성자를 생성해준다.
@AllArgsConstructor : 모든 필드값을 받는 생성자를 생성해준다.

-----------------------------------------------------------------------------
인터페이스로 repository를 만들고 extend JpaRepository를 상속 받는다면 proxy기술을 이용하여 spring-data-jpa가 관련 클래스들을 주입시켜준다
스프링 data jpa를 사용하면 @Repository 어노테이션을 사용하지않아도된다.

SPRING-DATA-JPA 주요메서드

1. save() :새로운 엔티티를 저장하는 메서드
2. delete() : 엔티티하나를 삭제, 내부에서 EntityManager.remove()가 호출다
3. findByID(ID) : 조회 메서드
4. getOne(ID) : 엔티티를 프록시로 조회한다
5. findAll() :모든 엔티티를 조회한다. 정렬이나 페이징조건을 파라미터로 제공할 수 있다.


●쿼리 메소드
1. 메소드 이름으로 쿼리를 생성해줌

순수 JPA를 사용하여 파라미터로 받은 age보다 age가 많은 사람의 이름을 가져오는 메소드는 아래처럼 복잡하게 짜야한다.

public List<Member> findByUsernameAndAgeGreaterThan(String username, int age) {
 return em.createQuery("select m from Member m where m.username = :username 
and m.age > :age")
 .setParameter("username", username)
 .setParameter("age", age)
 .getResultList();
} 

하지만 spring-data-jpa를 이용하면
 
List<Member> findByUsernameAndAgeGreaterThan(String username, int age); 이렇게 하면 충분하다. 메소드 이름을 분석하여 JPQL에 적용해주는 매우 편리한 기능을 가지고 있다.

findBy : 해당 엔티티에 대해 SELECT FORM하겠다
Username : 엔티티가 가지고 있는 변수명중에 내가 찾고싶은 것은 username이다
And : findBy뒷부분은 where절을 나타내는데 여러 조건을 연결시키기위한 연결부
AgeGreateThan : 내가 설정한 나이보다 더 큰 나이를 가진사람을 찾기위한 조건

https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation 
 
여러 가지 지원하는 케이스들을 볼 수 있음.
 
2. 조회시 : findBy, readBy, getBy, queryBy등의 메소드 사용가능
3. COUNT : countBy라고 하면 long으로 반환해준다
4. EXISTS : existsBy라고 하면 boolean으로 반환해준다
5. 삭제시 : deleteBy, removeBy
6. LIMIT: findFirst3, findFirst, findTop, findTop3 조회시 먼저 조회된 3개까지만 가져온다는 의미

●@Query
 
간단간단한 쿼리를 정의할 때는 메소드이름으로 쿼리를 정의하는 spring-data-jpa의 방식을 쓰면되지만 복잡하고 길어지는 쿼리에서는 명칭을 정확히 정의하기 힘들기 때문에
내가 원하는 새로운 메서드이름을 만들어서 사용하는 방법도 좋은 방법이다.
 
@Query("select m from Member m where m.username= :username and m.age = :age")
List<Member> findUser(@Param("username") String username, @Param("age") int
age);


 ●페이징

페이징 처리를 하더라도 엔티티를 그대로 외부로 보내버리면 안되기 때문에 아래의 방법을 사용하여 Dto에 저장한후 보내면 안전하게 사용할 수 있다.
 PageRequest pageRequest = PageRequest.of(1, 3, Sort.by(Sort.Direction.DESC, "username"));

Page<Member> page = memberRepository.findByAge(age,pageRequest);

Page<MemberDto> map = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

-> Page를 사용하면 자동으로 count메서드가 안에 들어있기 때문에 page변수에 .getTotalElements()를 사용하면 따로 count메서드를 설정하여 개수를 구하려 하지않아도 총 개수를 구할 수 있다.

●벌크성 수정 쿼리
특정 조건을 만족 할 때 update쿼리를 이용하여 변화를 주는 방법
뭐 어떤 페이지를 눌렀을 때 조회수가 오른다거나 하는 곳에 사용하면 좋을 듯
MemberJpaRepository에 저장되있다 bulkAgePlus로.. 까먹을 수도 있으니까

spring-data-jpa를 사용하여 만들 때는 쿼리는 똑같지만 꼭 위에 @Modifying을 붙여줘야한다.

!!!!주의할 점 : 벌크연산을 수행하는 것은 영속성 컨텍스트 개념을 가지고 연산을 하는 것이 아니라 바로 DB를 가지고 연산을 때려버리는 것이기 때문에 영속성 컨텍스트는 연산이 되었다는 사실조차 인지하지 못하게 된다. 그리하여 DB에는 변경된 정보가 담겨져있지만 sysout을 이용하여 직접 노출시켜보면 값이 변화되지않은 상태로 저장되어있는 것을 확인 할 수 있다.

해결법 : 벌크 연산을 수행한 후에는 반드시 EntityManger를 이용하여 영속성 컨텍스트를 비워줘야한다.

1. 첫 번째 방법
 
em.flush();
em.clear(); 이 두연산을 이용하여 영속성컨텍스트를 비워내면 다시 깔끔하게 DB에 들어있는 정보를 다시 조회하여 오기 때문에 영속성컨텍스트에도 저장된 정보가 저장되게 된다.

2. 두 번째 방법
 
@Modifying(clearAutomatically =true)modify에 이렇게 넣으면 자동으로 클리어를 해준다 편리한것같은 방법 하지만 원리는 첫 번째 방법을 편하게 해줄 뿐 첫 번째 방법을 인지하자

 ●★패치 조인 : 연관 관계가 있는 것들을 join을 이용하여 한번에 다가져와서 조회하는 방법

전제 조건 : 나는 member에 대한 정보를 조회하려고한다. 그리하여 List<Member> members = memberRepository.findAll();을 이용하여 member를 찾았는데
다른 정보들은 한번에 가져올 수 있지만 연관 관계에 있는 Team에 대한 정보가 Lazy로 설정되어있기 때문에 한번에 가져올 수가 없어서 한번더 조회해야한다.
만약 맴버가 한두명이라면 상관없지만 100명 200명이라면 맴버한명이 조회될때마다 team이 조회되고 만약 연관된 엔티티가 더있다면 계속해서 반복적으로 조회되게 된다. 그리하면 성능이 굉장이 나빠질 것이다.
이러한 것을 N + 1 문제라고 한다고 한다.

해결 방법 : 패치조인을 이용하여 해결한다.
 
ex)
@Query("select m from Member m join fetch m.team")
 
 List<Member> findMemberFetchJoin();

 
이런식으로 가짜 객체인 프록시를 사용하여 저장했다가 다시 조회하는 비효율적인 방법이 아닌 싹다 한번에 가져와서 조회를 한번에 수행하기 때문에 네트워크를 들낙날락하는 것이 매우 줄어들게 된다.

 ※하지만 spring-data-jpa는 더편하게 패치조인을 할 수 있는 방법을 제공해준다

 @EntityGraph(attributePaths = {"team"})
   
  List<Member> findAllMembers(); 
 
 이렇게 EntityGraph를 사용하면 더욱 간단하게 가능하다 arrtibutePaths에 추가한 team은 Member엔티티에 설정한 연관된 엔티티의 변수명을 적으면 된다.


●Auditing : 엔티티를 생성,변경할 때 변경한 사람과 시간을 추적하기 위해 만드는 것

1. 순수 JPA를 이용하여 만들기
생성날짜, 수정날짜에 관한 정보를 담고있는 클래스를 만들고 전체클래스에 @MappedSuperclass 어노테이션을 붙여준다.
그 후에 내가 만들어놓은 Member엔티티같은곳에 extends 날짜 클래스를 넣으면 테이블이 생성 될 때 자동으로 같이 들어가게 된다.


회원가입이라던지 엔티티를 만들었을 때의 시간을 알기위해서는
 
@PrePersist -> DB에 해당 테이블의 insert연산을 실행할 때 같이 실행되게하는 어노테이션
 
    @PrePersist
    public void prePersist(){
        LocalDateTime now = LocalDateTime.now();
        createdDate = now;
        updatedDate = now;
    }
 
이것은 persist하기전에 즉 DB에 저장되는 create문이 실행되기전에 시간을 저장하는 것이고

@PreUpdate -> DB에 해당 테이블의 update연산을실행할 때 같이 실행되게하는 어노테이션
 
    @PreUpdate
    public void preUpdate(){
        updatedDate = LocalDateTime.now();
    }
 
이것은 update하기전에 즉 DB에서 update문을 수행하기전에 시간을 저장하고 사용하려는 DB에 저장하는 것이다

지금은 시간만설정했지만 사용자나 다른편의상의 것들을 넣어놓으면 유지보수가 쉬워지고 추적에 용이하게 된다.

@PrePersist는 저장하기이전, @PostPersist는 저장하기 이후

2. spring-data-jpa를 이용하여 만들기

일단 어플리케이션 실행 클래스에
!!!!! @EnableJpaAuditing을 꼭 달아줘야한다.!!!!!

 ------------------------------------------------------------------------
@Getter 
 
@MappedSuperclass 
 
@EntityListeners(AuditingEntityListener.class)
 
public class BaseEntity {
 
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate; 
}
 ----------------------------------------------------------------------
 
이렇게만하면 간단하게 사용할 수 있다 extend바꿔주는거 잊지말기

 ● @PostConstruct란?

의존성 주입이 이루어진 후 초기화를 수행하는 메서드
이 어노테이션이 붙은 메서드는 다른 컨트롤러나 service에 의해 호출되지 않더라도 수행된다.(MemberController에 있음)

●한페이지에 페이징 정보가 둘 이상이면 접두사로 구분할 수 있다.

@Qualfier(“member”) Pageable memberPageable, @Qualfier(“order”) Pageable orederPageable 이렇게 pabeable 앞에 붙여주면 각각의 페이징정보를 사용할 수 있다.

●Projections

보통 findById같은 것을 해도 select *from 엔티티 m where id = :m.id 뭐이런식으로 전체를 조회하는데 전체를 조회하여 내가 원하는 내용물을가지고 오는 것이 아니라 정말 내가원하는 것들만 조회하고 싶을 때 사용할 수 있는 기능이다.
즉 select절 뒤에 들어갈 내용을 정의하여 편하게 사용하는 것(UsernameOnlyDto, AgeOnlyDto로 연습해봄)


