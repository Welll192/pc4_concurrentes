import threading
import math
import random
import re
import socket
import threading

class TCPClient50:
    def __init__(self, ip, listener):
        self.servermsj = None
        self.SERVERIP = ip
        self.SERVERPORT = 4444
        self.mMessageListener = listener
        self.mRun = False
        self.out = None
        self.incoming = None

    def sendMessage(self, message):
        if self.out is not None:
            self.out.write(message + '\n')
            self.out.flush()

    def stopClient(self):
        self.mRun = False

    def run(self):
        self.mRun = True
        try:
            serverAddr = socket.gethostbyname(self.SERVERIP)
            print("TCP Client - C: Conectando...")
            clientSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            clientSocket.connect((serverAddr, self.SERVERPORT))
            try:
                self.out = clientSocket.makefile('w')
                print("TCP Client - C: Sent.")
                print("TCP Client - C: Done.")
                self.incoming = clientSocket.makefile('r')
                while self.mRun:
                    self.servermsj = self.incoming.readline()
                    if self.servermsj is not None and self.mMessageListener is not None:
                        self.mMessageListener(self.servermsj)
                    self.servermsj = None
            except Exception as e:
                print("Minero en python trabajando")
            finally:
                clientSocket.close()
        except Exception as e:
            print("TCP - C: Error", e)

class Cliente50:
    def __init__(self):

        self.mTcpClient = None
        self.sc = None

    def main(self):
        objcli = Cliente50()
        objcli.iniciar()

    def iniciar(self):
        def run():
            def message_received(message):
                self.ClienteRecibe(message)

            self.mTcpClient = TCPClient50("127.0.0.1", message_received)
            self.mTcpClient.run()

        thread = threading.Thread(target=run)
        thread.start()

        print("Cliente bandera 01")
        while True:
            salir = input()
            self.ClienteEnvia(salir)
            if salir == "s":
                break
        print("Cliente bandera 02")


    def ClienteRecibe(self,llego):
        if "evalua" in llego.strip():
            arrayString = llego.split()
            shaEncontrado = bool(arrayString[4] != "")

            palabra = arrayString[1]
            if not shaEncontrado:
                dificultad = int(arrayString[2])
                nroMinero = int(arrayString[3])
                self.procesar(palabra, dificultad, nroMinero)
            else:
                sha = arrayString[2]
                nonce = arrayString[3]
                nroMinero = 3
                self.verifica(palabra, sha, nonce, nroMinero)

    def ClienteEnvia(self, envia):
        if self.mTcpClient is not None:
            self.mTcpClient.sendMessage(envia)


    def verifica(self,palabra, sha, nonce, nroMinero):
        miner = Miner(palabra, nonce)
        shaGenerado = miner.getHash()
        elapsedSeconds = miner.getElapsedSeconds() or 0
        result = miner.getResult() or ""
        nonceValue = miner.getNonce() or ""
        wordValue = miner.getWord() or ""
        if shaGenerado == sha:
            self.ClienteEnvia(f"rpta -> Minero: {nroMinero or ''} {elapsedSeconds or 0} {result or ''} {nonceValue or ''} {wordValue or ''} True")
        else:
            self.ClienteEnvia(f"rpta -> Minero: {nroMinero or ''} {elapsedSeconds or 0} {result or ''} {nonceValue or ''} {wordValue or ''} True")


    def procesar(self,palabra, dificultad, nroMinero):
        miner = Miner(palabra, dificultad)
        thread = Thread(target=miner.run)
        thread.start()
        try:
            thread.join()
        except Exception as e:
            print(e)
        elapsedSeconds = miner.getElapsedSeconds() or 0
        result = miner.getResult() or ""
        nonceValue = miner.getNonce() or ""
        wordValue = miner.getWord() or ""
        self.ClienteEnvia(f"rpta -> Minero: {nroMinero or ''} {elapsedSeconds or 0} {result or ''} {nonceValue or ''} {wordValue or ''} True")



import hashlib
import random
import string
import time

class Miner:
    def __init__(self, serverWord, difficulty):
        self.serverWord = serverWord
        self.difficulty = difficulty
        self.word = None
        self.nonce = None
        self.result = None
        self.startTime = None
        self.endTime = None

    def run(self):
        self.startTime = time.time()

        while True:
            self.word = self.serverWord
            self.nonce = self.generateNonce()
            self.result = self.calculateHash(self.word, self.nonce)

            if self.startsWithZeros(self.result, self.difficulty):
                self.endTime = time.time()
                break

    def startsWithZeros(self, string, numZeros):
        return string.startswith('0' * numZeros)

    def calculateHash(self, word, nonce):
        data = word + nonce
        digest = hashlib.sha256(data.encode()).hexdigest()
        return digest

    def getHash(self):
        return self.calculateHash(self.serverWord, self.nonce)

    def getWord(self):
        return self.word

    def getNonce(self):
        return self.nonce

    def getResult(self):
        return self.result

    def generateNonce(self):
        characters = string.ascii_letters + string.digits
        nonce = ''.join(random.choice(characters) for _ in range(8))
        return nonce

    def getElapsedSeconds(self):
        if self.endTime is None:
            return 0
        return self.endTime - self.startTime

# Ejecutar el programa
cliente = Cliente50()
cliente.main()
