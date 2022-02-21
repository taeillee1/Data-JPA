@NoArgsConstructor : 파라미터가 없는 기본 생성자를 생성해준다.
@AllArgsConstructor : 모든 필드값을 받는 생성자를 생성해준다.

-----------------------------------------------------------------------------
인터페이스로 repository를 만들고 extend JpaRepository를 상속 받는다면 proxy기술을 이용하여 spring-data-jpa가 관련 클래스들을 주입시켜준다
스프링 data jpa를 사용하면 @Repository 어노테이션을 사용하지않아도된다.

SPRING-DATA-JPA 주요메서드
1. save() :새로운 엔티티를 저장하는 메서드
2. delete() : 엔티티하나를 삭제, 내부에서 EntityManager.remove()가 호출돋ㄴ다
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


