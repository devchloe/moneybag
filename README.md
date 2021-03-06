# 카카오페이 뿌리기 기능 구현 전략

## 요구사항 구현 시 고려사항 
- 돈 뿌리기에 설정된 금액보다 상당한 금액이 초과 지출되지 않아야 한다.
- 최대한 돈 받기를 요청한 순서대로 처리해야 한다.

## 테이블 구조
- 돈 뿌리기 건 : 돈 받기 건 = 1 : N
- 돈 뿌리기 건은 0개 이상의 돈 받기 건을 갖는다
- 돈 받기 건은 반드시 하나의 돈 뿌리기 건에 속한다
- 돈 뿌리기 테이블과 돈 받기 테이블은 필수적 식별 관계

## 구현 방법
### 돈 뿌리기 데이터 잠금 설정과 돈 받기 순서 유지
- 돈 뿌리기를 실행 했을 때 공평하게 나눠줄 금액을 계산하여 돈 뿌리기 테이블에 저장한다.
- 돈 받기를 클릭 했을 때 트랜잭션을 생성하고 roomId, token에 해당하는 돈 뿌리기 행 데이터에 잠금을 생성한다.
- 돈 뿌리기 잠금을 하지 않고서는 돈 받기 테이블에 데이터를 쓸 수 없다. 따라서 돈 뿌리기 데이터에 접근한 순서에 따라 돈을 받아가게 된다.
- 낙관적인 잠금을 이용하는 경우 동시에 많은 사용자가 받기를 요청 했을 때 많은 예외가 발생할 수 있고, 사용자에게 문제 해결을 요청하거나 프로그램이 성공할 때까지 반복 시도해야 하는 등 사용자 불편, 
  비효율적인 무한 요청 작업이 발생할 수 있다. 그리고 순서를 보장하기 어렵다.
- 또한 1명에게 돈을 뿌리기로 한 경우, 사용자 A가 B보다 먼저 요청을 시작했더라도 A의 결과가 commit 되지 않은 상태에서 B가 먼저 돈을 받아 갔을 때 A도 돈을 받아가게 되는 상황을 확인했다. 
- 결과적으로 돈 뿌리기와 돈 받기 데이터는 논리적으로 하나의 데이터이다. 돈 받기를 실행하더라도 돈 뿌리기 데이터에 영향이 가지 않도록 하고 돈 뿌리기 데이터에 접근하지 않는 이상 돈을 받아갈 수 
  없도록 제어한다.
- 돈 뿌리기 데이터는 생성 후 절대 변하지 않는다. 그러나 돈 뿌리기와 연관된 돈 받기 데이터들의 상태가 변했는지는 알 필요가 있다. 돈 뿌리기와 논리적으로 하나의 그룹인 돈 받기 데이터가 변경 되었는지를 
  명시적으로 확인할 수 있도록 돈 뿌리기 테이블에 버전 컬럼을 추가했다. 돈 받기 데이터를 쓰면 변경되면 해당 돈 뿌리기 데이터의 버전이 강제로 증가한다. 트랜잭션 A가 세션을 생성하고 잠금을 획득하려는 사이, 
  트랜잭션 B가 먼저 잠금을 획득하고 버전을 증가시켰다면 트랜잭션 A는 실패하게 함으로써 돈 받기 테이블의 데이터가 변경되었음을 알 수 있도록 했다. 이로써 돈 뿌리기 금액을 초과할만한 추가적인 요소를 제거한다.

### 돈을 받을 수 없는 상황
- 제약 조건에 어긋나는 경우 돈을 받아갈 수 없다.
  - 돈 뿌리기 건 만료 여부, 돈을 받을 수 있는 자격 유무, 받아갈 돈이 남아있는지 여부 등
- 두 명 이상의 사용자가 같은 roomId, token을 가진 돈 뿌리기 건에 받기를 요청 했을 때 이전 잠금이 해제(트랜잭션 커밋 혹은 롤백) 되기 전까지 돈 받기 테이블에 데이터를 쓸 수 없다.
- 다음 트랜잭션이 N초 이상 대기하지 않도록 타임아웃을 설정하고 예외를 발생시킨다. 사용자는 다시 돈 받기를 요청해야 한다. (받기 프로세스는 최대 10초 이내에 종료될 수 있다고 가정함)
- 돈을 뿌린 사람이 입력한 사람 수만큼 돈 받기 테이블에 데이터가 쌓이면 더 이상 돈을 받아갈 수 없다.