from runtime import Runtime
from console import Console

type A: void { i: int } | void { j: int }

service S(){
	execution{ single }
	
	embed Runtime as Runtime
	embed Console as Console

	main{
		a = 10
		a.b = 10
		a.c = 10

		d = a

		dumpState@Runtime()(s1)
		print@Console(s1)()
	}
}