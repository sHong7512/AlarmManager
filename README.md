# AlarmManager

# 유휴상태에서 알람 울리는지 확인 방법
```
adb shell dumpsys battery unplug
```
```
adb shell dumpsys deviceidle step
```

# adb setting

- Path (Mac) (On Terminal) 
```
~ % open -e .bash_profile
```
and add path
```
# adb path
export PATH=$PATH:/Users/shong/Library/Android/sdk/platform-tools/
```
~ % source ~/.bash_profile

(adb 동작확인)

~ % adb version     

(adb devices 확인)

~ % adb devices     


# 알람 종류

- set
API 19이후, inexact 하게 동작한다. 연기되고 시간이 지난 이후에 전달된다.
OS에서 전체 시스템 알람을 "batch"하는 정책을 사용한다.
device가 wake up 하는 것을 최소화하고 배터리 사용량을 최적화한다.
실제 알람의 순서도 일치하지 않는다.
정확한 순서가 필요하면, setWindow, setExact를 써라.


- setExact
OS가 전달 시간 최적화하지 않는다.
요청된 시간에서 가능한 한 가까운 시간내에 전달한다.


- setAndAllowWhileIdle
doze 모드일 때도 실행되는 set → 배치 정책을 사용한다.
실제로 doze 모드일 때 실행 되야하는 알람의 경우에만 다루자.
ex) 일정이 되었다는 알람이 오는 캘린더
doze 모드일 때 동작해야 하므로, 과도한 사용을 막았다.
특정 앱의 빈도수 제한이 있다. (15분 정도?)


- setExactAndAllowWhileIdle
setExact + setAndAllowWhileIdle


- setIneactRepeating
반복적으로 실행되는 알람을 예약한다.
시스템에서 배치한다.


- setRepeating

반복적으로 실행되는 알람을 예약한다.
알람이 지연되면 가능한 한 빨리 스킵된 알람이 전달된다.
전달이 늦어져도 스케쥴은 정상적으로 동작한다.
한 시간마다 repeating, 7:45부터 8:45까지 폰이 자고 있다면 (8시 발생 알람 스킵)
8시 45분에 폰을 킬 때, 8시 발생 알람이 울리고
다음 알람은 9시에 잡힌다. (9시 45분이 아님!)

⇒ 실제 발생 시간 기준 몇 시간 후 반복되는 알람을 하고 싶다면 일회성 알람을 사용해 자체 스케줄 할 것

API 19 이후, inexact 하게 동작한다. 연기되고 시간이 지난 이후에 전달된다.


- setAlarmClock
AlarmClockInfo로 표현되는 시간에 알람을 예약한다.
알람이 실행될 때, 디바이스를 깨우고, 사용자에게 알림을 주는 용도로 사용한다.
화면을 키고, 소리를 재생하고, 진동을 울리고...

#주의! Doze모드일때

표준 AlarmManager 알람(setExact() 및 setWindow()포함)이 다음 유지보수 기간으로 연기

- 잠자기 모드에서 실행되는 알람을 설정해야한다면
```
setAndAllowWhileIdle() 또는 setExactAndAllowWhileIdle()
```

- setAlarmClock()으로 설정된 알람은 계속 정상적으로 실행됨. 시스템에서 이 알림이 싱행되기 직전에 잠자기 모드를 종료함

- 일반적인 알람은 set, setRepeating을 사용할 것. 배터리 효율은 OS의 배치 알고리즘에 의해 최적화됨

- doze 모드에서도 동작하려면 setAndAllowWhileIdle, setExactAndAllowWhileIdle를 사용할 것. 배터리 효율을 위한 OS 배치 알고리즘은 동작됨

- 알람 앱처럼 완벽하게 엄격한 알람이 필요한 경우는 setAlarmClock을 사용할 것
