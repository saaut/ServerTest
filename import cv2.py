import socket
def write_utf8(s, sock):
    encoded = s.encode(encoding='utf-8')
    sock.sendall(len(encoded).to_bytes(4, byteorder="big"))
    sock.sendall(encoded)

def get_bytes_stream(sock, length):#길이를 받고, 이미지를 읽어들이는 함수
    buffer = 'b'#byteArray를 누적해서 받을 버퍼 선언
    try:
        remain=length
        while True:
            data=sock.recv(remain)
            buffer +=data
            if len(buffer) < length:
                remain = length-len(buffer)#바이트의 일부분을 받을 때마다 받은 만큼을 뺀다
    except Exception as e:
        print(e)
    return buffer[:length]#버퍼의 길이가 이미지 바이트의 길이와 같아짐


host = '192.168.139.39' # Symbolic name meaning all available interfaces
port = 9999 # Arbitrary non-privileged port
 

server_sock = socket.socket(socket.AF_INET)
server_sock.bind((host, port))
server_sock.listen(1)
result =''

while True:
    print("기다리는 중")
    client_sock, addr = server_sock.accept()#client가 해당 서버에 connection을 요청하면 서버가 이를 받아서 클라이언트를 위한 소켓을 생성한다.
    #해당 클라이언트의 주소를 addr에 저장한다.

    print('Connected by', addr)

    # 서버에서 "안드로이드에서 서버로 연결요청" 한번 받음

    len_bytes_string = bytearray(client_sock.recv(1024))[2:]#안드로이드로부터 바이트 이미지의 길이를 전송받는다.
    #안드로이드에서 integer를 string으로 변환했으므로 bytearray를 변환한 뒤 3번째 인덱스부터 디코딩을 해줘야 한다.
    len_bytes = len_bytes_string.decode("utf-8")#함수의 바이트 길이를 먼저 받고, 모든 바이트들을 다 읽어들일때까지 while문을 반복한다.
    length = int(len_bytes)

    img_bytes =get_bytes_stream(client_sock,length)

    with open(img_bytes,"wb") as writer:
        writer.write(img_bytes)
    print('img is saved')
        
    write_utf8(result, client_sock)
        
    client_sock.close()

    server_sock.close()
