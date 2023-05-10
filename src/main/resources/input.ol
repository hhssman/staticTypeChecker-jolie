from .typehints import Typehint
from console import Console

service MyService() {
	embed Typehint as T
	embed Console as Console

	main {
		a = 10
		assert@T(a instanceof string)
		print@Console(a)()
	}
}