from runtime import Runtime
from console import Console

type A: void { i: int } | void { j: int }

service S(){
	execution{ single }
	
	embed Runtime as Runtime
	embed Console as Console

	main{
		a = "hey" + true
		b = "hey" - true
		c = "hey" * true
		d = "hey" / true
		e = "hey" % true

		dumpState@Runtime()(s1)
		print@Console(s1)()
	}
}