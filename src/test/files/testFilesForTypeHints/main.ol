from .assertions import Assertions, AssertionsInterface

type Weird: int {
	y: Weird | int
}

service MyService{
	outputPort lolleren{
		interfaces: AssertionsInterface
	}

	embed Assertions in lolleren

	main {
		x = 10

		while(true){
			x.y << x
			assert@lolleren(x instanceof Weird)
		}
	}
}
