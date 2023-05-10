from .assertions import Assertions, AssertionsInterface

type Weird: int {
	y: Weird | int
}

service MyService{
	outputPort lolleren{
		interfaces: AssertionsInterface
	}

	embed Assertions as assertions

	main {
		// x = 10

		// while(true){
		// 	x.y << x
		// 	assert@lolleren(x instanceof Weird)
		// }

		a = 10
		a.x = "Hello"
		assert@assertions(a instanceof int)
	}
}
