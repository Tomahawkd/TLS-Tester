import sys
import requests
from bs4 import BeautifulSoup

fingerprint = sys.argv[1]

s = requests.Session()
s.get("https://censys.io/ipv4?q={fingerprint}&")

header = {
	"User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_4) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1 Safari/605.1.15",
	"Referer": f"https://censys.io/ipv4?q={fingerprint}&",
	"X-Requested-With": "XMLHttpRequest"
}

s.headers.update(header)

r = s.get(f"https://censys.io/ipv4/_search?q={fingerprint}&")

if r.text.startswith("{"):
	print(r.text)
	exit(1)

ip = BeautifulSoup(r.text, 'html.parser').find_all("span", attrs={"class": "dns"})
for i in ip:
	print(i["id"])
exit(0)