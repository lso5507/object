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
### 객체지향설계에서도 Screening, DIscountPolicy 등 여러 객체를 의존하던데요
해당 객체가 없다면 절차지향, 객체지향 둘다 문제되지 않나요?

- 여기서 말하는 “의존” 은 해당 도메인 객체들이 변경되었을 때 Service 객체에 영향이 가냐는 의미임
실제로 calculateDiscount(), isSatifiedBy() 등 도메인 객체 내에 할인정책, 할인금액 계산에 대한 기능변경이 발생하여도 ReservationService는 영향받지 않으므로 의존하지 않는다. 라고 보는 것임
    - 여기서 객체지향에서 캡슐화가 중요한 이유가 보여지네요.

### 잘보면…

- 이미 위에서 언급한 요구사항과 도메인은 같은 모양이어야 한다가 일치되었음

## 캡슐화는 왜 할까?

[객체지향설계를 왜 해야하는가?](https://www.notion.so/275973beb62580b89a55df4bc7de869a?pvs=21)


# GRASP

- 객체지향 디자인 패턴
- 객체에게 책임을 할당할 때 어떤 객체가 해당 책임을 갖는게 가장 적합한가?

## 정보전문가(**Information expert)**

- 책임 수행을 위한 정보를 가장 많이 가지고 있는 객체에게 책임을 할당
- e.g) 영화 예매에 대한 책임을 할당할 때 해당 영화의 상영시간, 인원, 가격, 할인가, 금액, 영화 제목 에 대한 정보를 가장 많이 알고있는 상영 객체에게 책임을 할당

### 영화에 대한 내용인데 영화 객체가 책임을 가지는 게 맞지 않습니까?

- 상영 객체는 영화의 객체와 협력 관계이므로 영화에서 얻을 수 있는 정보는 상영을 통해 얻을 수 있음

### 그것은 영화 객체에 책임을 할당을 하여도 똑같은 논리 아닙니까?

- 내 생각도 그럼. 그 말은 즉 영화 객체에 책임이 할당되어도 된다는 의미 같음
    - 책임을 할당한 데에 합당한 근거만 있다면 문제가 될게 없다고 봄
        - 주관적인 해석이 들어간 설계 관점이기 때문

## 창조자 패턴(Creator)

- 새로운 인스턴스를 생성하는 책임을 어떤 객체에게  할당할 것인가?
- 다음 중 한가지라도 만족할 경우 A 인스턴스를 생성할 책임을 B에게 할당하라
    - B가 A를 포함하거나 참조한다
        - 상영은 이 조건을 만족함
    - B가 A를 기록한다
    - B가 A를 긴밀하게 사용한다
    - B가 A를 초기화하는 데 필요한 정보를 알고 있다
        - 영화, 상영은 이 조건을 만족함

<img width="1558" height="922" alt="image" src="https://github.com/user-attachments/assets/f96e50dd-6db2-424f-afe2-4e6b6d7befa2" />


### 낮은 결합도 (Low Coupling)

- 예매에 대한 책임을 영화가 갖는다면 영화와 예매의 결합이 생김

  ### 높은 응집도 (High Cohesion)

- 한 요소에 책임들이 얼마나 관련되게 집중되어 있는가?

```java
class Movie {
	private DiscountPolicy discountPolicy;
	public Money calculateFee(...) {
		if (금액할인조건이라면) {
			return discountPolicy.calculateAmountDiscount(...);
		}
		//else if(새로운 할인조건) 
		return discountPolicy.calculatePercentDiscount(...);
	}
}
```

```java
class DiscountPolicy {
	public Money calculatePercentDiscount(...) {
		...
	}
	public Money calculateAmountDiscount(...) {
		...
	}
}
```

- 새로운 할인정책(DIscountPolicy)의 추가로 Movie, DiscountPolicy의 코드수정이 발생한다는 것은
**여러 책임(정책 선택, 분기, 계산 로직)을 뒤섞어** 가지고 있다는 신호
- 또한 OCP 원칙도 위배함
    - OCP : 수정엔 닫혀있고, 확장에는 열려있어야 함

## 변경보호패턴

- 변화가 예상되거나 불안정한 지점을 예상하고 그 지점을 추상화 하는 것
- 위에 예제에서는 DiscountPolicy가 변경보호패턴 대상
    - 새로운 할인정책이 있을경우 DiscountPolicy는 변화되어야 함
## 간접화

- 다른 컴포넌트나 서비스가 직접 의존하지 않도록 중재하는 중간 객체에 책임할당
- 도메인 객체와 데이터베이스 사이의 의존성 제거

## 결합도

- 모듈이 다른 외부모듈에 의존하는 정도

<img width="1560" height="1002" alt="image" src="https://github.com/user-attachments/assets/ea8d3a98-787b-4cd1-aa13-b094108a24f0" />


- 자주 변하는 구현 부분이 아닌, 안정적인 추상화 부분에 의존

### 절차지향코드

```java
public class ReservationService {
	private Money calculateDiscount(DiscountPolicy policy, Movie movie) {
	if (policy.isAmountPolicy()) {
		return Money.wons(policy.getAmount());
	} else if (policy.isPercentPolicy()) {
		return movie.getFee().times(policy.getPercent());
	}
		return Money.ZERO;
	}
}
public class DiscountPolicy {
	private Long movieId;
	private PolicyType policyType;
	private Long amount;
	private Double percent;
	public Long getMovieId() { return movieId; }
	public void setMovieId(Long movieId) { this.movieId = movieId; }
	public PolicyType getPolicyType() { return policyType; }
	public void setPolicyType(PolicyType policyType) { this.policyType = policyType; }
	public boolean isPercentPolicy() { return this.policyType == PERCENT; }
	public boolean isAmountPolicy() { return this.policyType == AMOUNT; }
	public Long getAmount() { return amount; }
	public Double getPerent() { return percent; }
	public void setAmount(Long amount) { this.amount = amount; }
	public void setPercent(Double percent) { this.percent = percent; }
}
```

<img width="2012" height="1174" alt="image" src="https://github.com/user-attachments/assets/5c44d8eb-0b0a-46da-bc0d-67aa7111a5e3" />


- 자주 변하는 필드는 캡슐화
- 외부객체가 자주 변하지않는 메소드 시그니쳐에 의존하도록 함

### amount 필드의 타입이 변경된다면?

```java
public class DiscountPolicy {
	private Long movieId;
	private PolicyType policyType;
	private Double amount;  // Long -> Double
	private Double percent;
	public Long getMovieId() { return movieId; }
	public void setMovieId(Long movieId) { this.movieId = movieId; }
	public PolicyType getPolicyType() { return policyType; }
	public void setPolicyType(PolicyType policyType) { this.policyType = policyType; }
	public boolean isPercentPolicy() { return this.policyType == PERCENT; }
	public boolean isAmountPolicy() { return this.policyType == AMOUNT; }
	public Double getAmount() { return amount; } // 메소드 시그니쳐도 변경 됨
	public Double getPerent() { return percent; }
	public void setAmount(Long amount) { this.amount = amount; }
	public void setPercent(Double percent) { this.percent = percent; }
}
```

- 그러므로 외부객체가 amount에 대한 타입으로 반환이 되면 높은 결합도가 발생 됨
→ 변경점이 생기기 떄문

### 객체지향코드

```java
public class Movie {
	private DiscountPolicy discountPolicy;
	public Money calculateFee(Screening screening) {
		return fee.minus(discountPolicy.calculateDiscount(screening));
	}
}

public abstract class DiscountPolicy {
	private List<DiscountCondition> conditions;
	public Money calculateDiscount(Screening screening) {
		for(DiscountCondition each : conditions) {
			if (each.isSatisfiedBy(screening)) {
				return getDiscountAmount(screening);
			}
}
	return Money.ZERO;
}
	abstract protected Money getDiscountAmount(Screening screening);
}

public class PercentDiscountPolicy extends DiscountPolicy {
	private Double percent;
	protected Money getDiscountAmount(Screening screening) {
		return screening.getFixedFee().times(percent);
	}
}
```

<img width="1816" height="1214" alt="image" src="https://github.com/user-attachments/assets/d7e82908-2dee-46e9-813c-d25086092ec3" />

- DiscountPolicy.calculateDiscount(Screening) 으로 객체를 반환하는 메소드 시그니처를 만든다면?
→ Money 라는 VO로 반환되면 되므로 내부 구현코드가 변경되어도 Movie는 변경사항이 없음
그렇다면 낮은 결합도가 유지가 됨


## 타입 캡슐화

- 외부에서 해당 타입을 알 수 없도록 캡슐화 하는 것을 의미

### 타입 캡슐화 되지 않은 코드

```java
public class Movie{
	private AmountDiscountPolicy amountDiscountPolicy;
	private PercentDiscountPolicy percentDiscountPolicy;
}
```

- DiscountPolicy의 종류가 변경된다면 Movie의 영향을 끼침
    - 높은결합도

```java
public class Movie{
	private DiscountPolicy discountPolicy;
}
public abstract class DiscountPolicy{}
```

- DIscountPolicy를 추상화(인터페이스) 하여 새로운 DiscountPolicy가 추가되어도 Movie에 영향이 가지않음

## 올바른 설계 평가

1. 같은 역할을 하는 새로운 클래스가 추가된다하여도 기존 코드가 변경되어선 안된다
    1. OCP
2. 해당 클래스를 생성할때 가지고 있는 필드를 전부 의미있는 값으로 저장하는가?
    1. 특정 시점에서만 사용되어 null로 선언되어야하는 필드가 있다면 잘못된 설계
    → 낮은 응집도 

## 변경된 요구사항

<img width="1558" height="922" alt="image" src="https://github.com/user-attachments/assets/8c1b196a-90a5-4544-b85d-52807758ce92" />


- 기존 영화와 할인 정책의 연결은 1:1 이였으나 고객의 요청으로 1:N으로 변경되어야 함

### 객체지향적설계

```java

public class OverlappedDiscountPolicy extends DiscountPolicy {
	private List<DiscountPolicy> policies = new ArrayList<>();
	public OverlappedDiscountPolicy(DiscountPolicy... policies) {
		super((screening) -> true);
		this.policies = Arrays.asList(policies);
	}
	@Override
	protected Money getDiscountAmount(Screening screening) {
		Money result = Money.ZERO;
		for(DiscountPolicy each : policies) {
			result = result.plus(each.calculateDiscount(screening));
		}
		return result;
	}
}
```

- 중복할인이라는 Policy 객체를 만들어 중복할인 기능 구현
    - 기존 Movie 클래스는 DIscountPolicy를 의존하고 있으므로 코드수정이 발생하지 않음
