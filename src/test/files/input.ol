from .types import imp, MyInterface
// from .otherservice import EmbedInService, EmbedAsService, EmbedMeInterface1, EmbedMeInterface2




// type A: int {
// 	x: imp
// 	y: string
// }

// type B: int {
// 	x: C
// }

// type C: bool {
// 	x: double
// }

// type D: void {
// 	x: int | string
// 	y: bool | double
// 	z: long
// }

// type choice: int | string

type X: int {
	l: imp
}

type Y: int {?}

service MyService(input: X) {
	// inputPort in {
	// 	Location: "socket://localhost:8080"
	// 	Protocol: http { format = "json" }
	// 	Interfaces: MyInterface 
	// }
	
	outputPort out {
		Location: "socket://localhost:8081"
		Protocol: http { format = "json" }
		Interfaces: MyInterface
	}

	main {
		helloOneway@out(input)

		// [helloOneway(input)]
		// [helloReqRes(input)(output)]
	}
}