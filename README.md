**object**

조영호 오브젝트 기초편

https://www.inflearn.com/course/%EC%98%A4%EB%B8%8C%EC%A0%9D%ED%8A%B8-%EA%B8%B0%EC%B4%88%ED%8E%B8-%EA%B0%9D%EC%B2%B4%EC%A7%80%ED%96%A5/dashboard

## 도메인

- 요구사항 정의 아이템

## 요구사항

1. 영화 예매 서비스를 만들어야함
2. 사용자는 영화를 예매할 수 있어야함
3. 사용자는 예매 시 알맞는 할인 혜택을 받을 수 있어야하고 할인받은 금액으로 결제되어야 함
    1. 할인 방식은 정액, 정률이 있음

## 절차지향설계[예약]

https://github.com/eternity-oop/object-basic-02-01/blob/main/src/main/java/org/eternity/reservation/domain/DiscountCondition.java

```java
public class DiscountCondition {
    public enum ConditionType { PERIOD_CONDITION, SEQUENCE_CONDITION }

    private Long id;
    private Long policyId;
    private ConditionType conditionType;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer sequence;

    public DiscountCondition() {
    }

    public DiscountCondition(Long policyId, ConditionType conditionType, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, Integer sequence) {
        this(null, policyId, conditionType, dayOfWeek, startTime, endTime, sequence);
    }

    public DiscountCondition(Long id, Long policyId, ConditionType conditionType, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, Integer sequence) {
        this.id = id;
        this.policyId = policyId;
        this.conditionType = conditionType;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sequence = sequence;
    }

	 //getter.. setter
}
```

```java
public class DiscountPolicy {
    public enum PolicyType { PERCENT_POLICY, AMOUNT_POLICY }

    private Long id;
    private Long movieId;
    private PolicyType policyType;
    private Money amount;
    private Double percent;

    public DiscountPolicy() {
    }

    public DiscountPolicy(Long movieId, PolicyType policyType, Money amount, Double percent) {
        this(null, movieId, policyType, amount, percent);
    }

    public DiscountPolicy(Long id, Long movieId, PolicyType policyType, Money amount, Double percent) {
        this.id = id;
        this.movieId = movieId;
        this.policyType = policyType;
        this.amount = amount;
        this.percent = percent;
    }
    //getter.. setter...

    public boolean isAmountPolicy() {
        return PolicyType.AMOUNT_POLICY.equals(policyType);
    }

    public boolean isPercentPolicy() {
        return PolicyType.PERCENT_POLICY.equals(policyType);
    }

}
```

```java
public class Reservation {
    private Long id;
    private Long customerId;
    private Long screeningId;
    private Integer audienceCount;
    private Money fee;

    public Reservation(Long customerId, Long screeningId, Integer audienceCount, Money fee) {
        this(null, customerId, screeningId, audienceCount, fee);
    }

    public Reservation(Long id, Long customerId, Long screeningId, Integer audienceCount, Money fee) {
        this.id = id;
        this.customerId = customerId;
        this.screeningId = screeningId;
        this.audienceCount = audienceCount;
        this.fee = fee;
    }
//..getter ,setter
}
```

```java

public class ReservationService {
    private ScreeningDAO screeningDAO;
    private MovieDAO movieDAO;
    private DiscountPolicyDAO discountPolicyDAO;
    private DiscountConditionDAO discountConditionDAO;
    private ReservationDAO reservationDAO;

    public ReservationService(ScreeningDAO screeningDAO,
                              MovieDAO movieDAO,
                              DiscountPolicyDAO discountPolicyDAO,
                              DiscountConditionDAO discountConditionDAO,
                              ReservationDAO reservationDAO) {
        this.screeningDAO = screeningDAO;
        this.movieDAO = movieDAO;
        this.discountConditionDAO = discountConditionDAO;
        this.discountPolicyDAO = discountPolicyDAO;
        this.reservationDAO = reservationDAO;
    }

    public Reservation reserveScreening(Long customerId, Long screeningId, Integer audienceCount) {
        Screening screening = screeningDAO.selectScreening(screeningId);
        Movie movie = movieDAO.selectMovie(screening.getMovieId());
        DiscountPolicy policy = discountPolicyDAO.selectDiscountPolicy(movie.getId());
        List<DiscountCondition> conditions = discountConditionDAO.selectDiscountConditions(policy.getId());

        DiscountCondition condition = findDiscountCondition(screening, conditions);

        Money fee;
        if (condition != null) {
            fee = movie.getFee().minus(calculateDiscount(policy, movie));
        } else {
            fee = movie.getFee();
        }

        Reservation reservation = makeReservation(customerId, screeningId, audienceCount, fee);
        reservationDAO.insert(reservation);

        return reservation;
    }

    private DiscountCondition findDiscountCondition(Screening screening, List<DiscountCondition> conditions) {
        for(DiscountCondition condition : conditions) {
            if (condition.isPeriodCondition()) {
                if (screening.isPlayedIn(condition.getDayOfWeek(),
                                         condition.getStartTime(),
                                         condition.getEndTime())) {
                    return condition;
                }
            } else {
                if (condition.getSequence().equals(screening.getSequence())) {
                    return condition;
                }
            }
        }

        return null;
    }

    private Money calculateDiscount(DiscountPolicy policy, Movie movie) {
        if (policy.isAmountPolicy()) {
            return policy.getAmount();
        } else if (policy.isPercentPolicy()) {
            return movie.getFee().times(policy.getPercent());
        }

        return Money.ZERO;
    }

    private Reservation makeReservation(Long customerId, Long screeningId, Integer audienceCount, Money fee) {
        return new Reservation(customerId, screeningId, audienceCount, fee.times(audienceCount));
    }
}
```

### 의존성

<img width="602" height="518" alt="image" src="https://github.com/user-attachments/assets/e34cc6da-4a0d-4f6d-85cc-893f9cebb957" />


- 데이터 객체가 변경된다면 Service 객체도 변경되어야함

## 객체지향설계[예약]

https://github.com/eternity-oop/object-basic-02-04

```java
public class DiscountCondition {
    public enum ConditionType { PERIOD_CONDITION, SEQUENCE_CONDITION, COMBINED_CONDITION }

    private Long id;
    private Long policyId;
    private ConditionType conditionType;
    private DayOfWeek dayOfWeek;
    private TimeInterval interval;
    private Integer sequence;

    public DiscountCondition() {
    }

    public DiscountCondition(Long policyId, ConditionType conditionType, DayOfWeek dayOfWeek,
                             LocalTime startTime, LocalTime endTime, Integer sequence) {
        this(null, policyId, conditionType, dayOfWeek, TimeInterval.of(startTime, endTime), sequence);
    }

    public DiscountCondition(Long id, Long policyId, ConditionType conditionType, DayOfWeek dayOfWeek,
                             TimeInterval interval, Integer sequence) {
        this.id = id;
        this.policyId = policyId;
        this.conditionType = conditionType;
        this.dayOfWeek = dayOfWeek;
        this.interval = interval;
        this.sequence = sequence;
    }

    public boolean isSatisfiedBy(Screening screening) {
        if (isPeriodCondition()) {
            if (screening.isPlayedIn(dayOfWeek, interval.getStartTime(), interval.getEndTime())) {
                return true;
            }
        } else if (isSequenceCondition()){
            if (sequence.equals(screening.getSequence())) {
                return true;
            }
        } else if (isCombinedCondition()) {
            if (screening.isPlayedIn(dayOfWeek, interval.getStartTime(), interval.getEndTime()) &&
                    sequence.equals(screening.getSequence())) {
                return true;
            }
        }

        return false;
    }

    public Long getPolicyId() {
        return policyId;
    }

    private boolean isPeriodCondition() {
        return ConditionType.PERIOD_CONDITION.equals(conditionType);
    }

    private boolean isSequenceCondition() {
        return ConditionType.SEQUENCE_CONDITION.equals(conditionType);
    }

    private boolean isCombinedCondition() {
        return ConditionType.COMBINED_CONDITION.equals(conditionType);
    }
}
```

```java

public class DiscountPolicy {
    public enum PolicyType { PERCENT_POLICY, AMOUNT_POLICY }

    private Long id;
    private Long movieId;
    private PolicyType policyType;
    private Money amount;
    private Double percent;
    private List<DiscountCondition> conditions;

    public DiscountPolicy() {
    }

    public DiscountPolicy(Long movieId, PolicyType policyType, Money amount, Double percent) {
        this(null, movieId, policyType, amount, percent, new ArrayList<>());
    }

    public DiscountPolicy(Long movieId, PolicyType policyType, Money amount, Double percent, List<DiscountCondition> conditions) {
        this(null, movieId, policyType, amount, percent, conditions);
    }

    public DiscountPolicy(Long id, Long movieId, PolicyType policyType, Money amount, Double percent, List<DiscountCondition> conditions) {
        this.id = id;
        this.movieId = movieId;
        this.policyType = policyType;
        this.amount = amount;
        this.percent = percent;
        this.conditions = conditions;
    }

    public boolean findDiscountCondition(Screening screening) {
        for(DiscountCondition condition : conditions) {
            if(condition.isSatisfiedBy(screening)) {
                return true;
            }
        }

        return false;
    }

    public Money calculateDiscount(Movie movie) {
        if (isAmountPolicy()) {
            return amount;
        } else if (isPercentPolicy()) {
            return movie.getFee().times(percent);
        }

        return Money.ZERO;
    }

    public Long getId() {
        return id;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setDiscountConditions(List<DiscountCondition> conditions) {
        this.conditions = conditions;
    }

    private boolean isAmountPolicy() {
        return PolicyType.AMOUNT_POLICY.equals(policyType);
    }

    private boolean isPercentPolicy() {
        return PolicyType.PERCENT_POLICY.equals(policyType);
    }
}
```

```java
public class Reservation {
    private Long id;
    private Long customerId;
    private Long screeningId;
    private Integer audienceCount;
    private Money fee;

		public Reservation(){}
		
    private Reservation(Long customerId, Long screeningId, Integer audienceCount, Money fee) {
        this(null, customerId, screeningId, audienceCount, fee);
    }

    public Reservation(Long id, Long customerId, Long screeningId, Integer audienceCount, Money fee) {
        this.id = id;
        this.customerId = customerId;
        this.screeningId = screeningId;
        this.audienceCount = audienceCount;
        this.fee = fee;
    }

    public static Reservation makeReservation(Long customerId, Long screeningId, Integer audienceCount, Money fee) {
        return new Reservation(customerId, screeningId, audienceCount, fee.times(audienceCount));
    }
}
```

```java
public class ReservationService {
    private ScreeningDAO screeningDAO;
    private MovieDAO movieDAO;
    private DiscountPolicyDAO discountPolicyDAO;
    private DiscountConditionDAO discountConditionDAO;
    private ReservationDAO reservationDAO;

    public ReservationService(ScreeningDAO screeningDAO,
                              MovieDAO movieDAO,
                              DiscountPolicyDAO discountPolicyDAO,
                              DiscountConditionDAO discountConditionDAO,
                              ReservationDAO reservationDAO) {
        this.screeningDAO = screeningDAO;
        this.movieDAO = movieDAO;
        this.discountConditionDAO = discountConditionDAO;
        this.discountPolicyDAO = discountPolicyDAO;
        this.reservationDAO = reservationDAO;
    }

    public Reservation reserveScreening(Long customerId, Long screeningId, Integer audienceCount) {
        Screening screening = screeningDAO.selectScreening(screeningId);
        Movie movie = movieDAO.selectMovie(screening.getMovieId());
        DiscountPolicy policy = discountPolicyDAO.selectDiscountPolicy(movie.getId());
        boolean found = policy.findDiscountCondition(screening);

        Money fee;
        if (found) {
            fee = movie.getFee().minus(policy.calculateDiscount(movie));
        } else {
            fee = movie.getFee();
        }

        Reservation reservation = Reservation.makeReservation(customerId, screeningId, audienceCount, fee);
        reservationDAO.insert(reservation);

        return reservation;
    }

    private Reservation makeReservation(Long customerId, Long screeningId, Integer audienceCount, Money fee) {
        return new Reservation(customerId, screeningId, audienceCount, fee.times(audienceCount));
    }
```

### 의존성

<img width="1036" height="872" alt="image" src="https://github.com/user-attachments/assets/bee8f515-4d10-4a12-98fb-b77cd3ffa59d" />


- ReservationService.reserveScreening  메서드를 호출하여 영화 예매를 완료
- 도메인 객체(DIscountPolicy, DiscountCondition)에서 자체적으로 책임(행위)를 가지고 있음
- 새로운 할인정책(DIscountCondition)이 생기면 해당 도메인 객체에서 변경을 진행하면 됨
    - 할인정책이 추가된다면 findDiscountCondition 메서드를 변경해야함
    절차지향에선 Service, 객체지향에선 DIscountPolicy이 가지고 있었음

## 캡슐화는 왜 할까?
