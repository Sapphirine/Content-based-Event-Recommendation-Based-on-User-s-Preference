#coding=utf-8
import json
import sys
import urllib2, urllib
import re
import codecs
import json
import os
import socket
import threading 
import time
from bs4 import BeautifulSoup
socket.setdefaulttimeout(50)
reload(sys)
sys.setdefaultencoding('utf8')

tryTimes = 3
writeLock = threading.Lock()
writeDoneWorkLock = threading.Lock()





#type 0,justopen, 1,gb2312, 2,gbk, 3,GBK, 4,utf-8
def getPageWithSpecTimes(decodeType, url):
    global tryTimes
    alreadyTriedTimes = 0
    html = None
    while alreadyTriedTimes < tryTimes:
        try:
            if decodeType == 0:
                html = urllib.urlopen(url).read()                
            elif decodeType == 1:
                html = urllib.urlopen(url).read().decode('gb2312', 'ignore').encode('utf8')
            elif decodeType == 2:
                html = urllib.urlopen(url).read().decode('gbk', 'ignore').encode('utf8')
            elif decodeType == 3:
                html = urllib.urlopen(url).read().decode('GBK', 'ignore').encode('utf8')
            else:
                html = urllib.urlopen(url).read()
            break
        except Exception as ep:
            if alreadyTriedTimes < tryTimes - 1:
                alreadyTriedTimes += 1
                pass
            else:
                return None
    return html

def getPage(pageUrl, filename):
    pageContent = getPageWithSpecTimes(0, pageUrl)
    #filehandler = open('index.html', 'w')
    #filehandler.write(pageContent)
    #filehandler.close()
    jsonResult = None
    if 'eventsCollection:' in pageContent:    
        jsonPattern = re.compile(r'eventsCollection:(.+?)filterCollection:', re.S)
        jsonContent = jsonPattern.findall(pageContent)
        jsonResult = json.loads(jsonContent[0].strip()[:-1])
        #print jsonResult[0]
    elif 'mediatorjs.set(\'events\'' in pageContent:
        jsonPattern = re.compile(r'mediatorjs\.set\(\'events\',(.+?)mediatorjs\.set\(\'filtersData\'', re.S)
        jsonContent = jsonPattern.findall(pageContent)
        jsonResult = json.loads(jsonContent[0].strip()[:-2])
        #print jsonResult[0]

    else:
        exit()
    for eachEvent in jsonResult:
        try:
            eventName = eachEvent['name']['text']
            eventPrice = eachEvent['price_range']
            eventTime = eachEvent['start']['utc']
            eventAddress = eachEvent['venue']['longaddress']
            eventType = eachEvent['category']['name']
            eventOrganizer = eachEvent['organizer']['name'].strip()
            eventDescription = eachEvent['description']['text'].strip().replace('\n','')
            filename.write('%s;;%s;;%s;;%s;;%s;;%s;;%s\n' % (eventName, eventTime, eventPrice, eventAddress, eventType, eventOrganizer, eventDescription))
        except Exception as ep:
            pass


filehandler = open('result.txt', 'w')
for i in range(1,646):
    print i
    url = 'https://www.eventbrite.com/d/ny--new-york/events/?page=%s' % i
    getPage(url, filehandler)
filehandler.close()


