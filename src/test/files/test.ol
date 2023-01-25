from runtime import Runtime
from console import Console

type A: void { i: int } | void { j: int }

service S(){
	execution{ single }
	
	embed Runtime as Runtime
	embed Console as Console

	main{
		dumpState@Runtime()(s1)
		print@Console(s1)()

		print@Console("\n--------------\n")()

		param.choice.i = 10

		dumpState@Runtime()(s2)
		print@Console(s2)()
	}
}