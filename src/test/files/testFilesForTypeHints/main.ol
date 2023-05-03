from .assertions import Assertions, AssertionsInterface

type Weird: int {
	y: Weird | int
}

service MyService{
	outputPort assertions{
		interfaces: AssertionsInterface
	}

	embed Assertions in assertions

	main {
		x = 10

		while(true){
			x.y << x
			assert@assertions(x instanceof Weird)
		}
	}
}
