// from runtime import Runtime
// from console import Console
from .embedMe import EmbedMe

service MyService() {
	// embed Runtime as Runtime
	// embed Console as Console
	embed EmbedMe as lol

	main {
		a = 10
		// dumpState@Runtime()(s1)
		// print@Console(s1)()
	}
}