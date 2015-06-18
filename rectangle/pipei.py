#coding:utf-8
import re
import sys
import urllib
import os
import time

import cv
import numpy as np
from PIL import Image

np.set_printoptions(threshold='nan')

def sort(a):# 二维数组排序
    for k in range(len(a)):
        (a[k][0],a[k][1]) = (a[k][1],a[k][0])
    a.sort()
    for k in range(len(a)):
        (a[k][0],a[k][1]) = (a[k][1],a[k][0])
    return a

def difTowPic(image,tempImage):
    image = cv.LoadImage(image,0)
    template = cv.LoadImage(tempImage,0)
    cv.ResetImageROI(image)
    W,H=cv.GetSize(image)
    w,h=cv.GetSize(template)
    width=W-w+1
    height=H-h+1
    result=cv.CreateImage((width,height),32,1)
    # result 是一个矩阵，存储了模板与源图像每一帧相比较后的相似值，
    cv.MatchTemplate(image,template, result,cv.CV_TM_SQDIFF)
    # 下面的操作将从矩阵中找到相似值最小的点，从而定位出模板位置
    (min_x,max_y,minloc,maxloc)=cv.MinMaxLoc(result)
    (x,y)=minloc
    cv.Rectangle(image,(int(x),int(y)),(int(x)+w,int(y)+h),(0,0,0),1,0)

    resultArray = np.asarray(result[:])
    output = re.sub('[^\d]', '',tempImage)
    return [int(output),int(x)+w/2,np.min(resultArray)] # [数字，位置,结果中最小值]

def deleteNumPosition(imagePath,centerPosition):
    image = cv.LoadImage(imagePath,0)
    w = image.width
    h = image.height
    iHist = cv.CreateImage((w,h),8,1)
    for i in range(h):
        for j in range(w):
            iHist[i,j] = image[i,j]
    for m in range(h):
        for n in range(centerPosition-6,centerPosition+7):
            iHist[m,n] = 255
    cv.SaveImage(imagePath,iHist)
    return iHist

def oneSureNum(imagePath): # 得到最可靠的数字以及其中心位置，并画掉原来的数字
    oneNumArray = []

    for i in range(10):
        oneNumArray.append(difTowPic(imagePath,str(sys.argv[2])+"template/"+str(i)+".jpg"))

    b = oneNumArray[0] # 最小的值
    ci = 0
    for i in range(10):
        if(oneNumArray[i][2] < b[2]):
            b = oneNumArray[i]
            ci = i

    deleteNumPosition(imagePath,b[1])
    c = [] # 倒数第二小
    di = 0
    for i in range(10):
        if (i != ci ):
            c = oneNumArray[i]
            di = i
    for i in range(10):
        if(oneNumArray[i][2] != b[2]):
            if(oneNumArray[i][2] > b[2] and oneNumArray[i][2] < c[2]):
                c = oneNumArray[i]

    d = [] # 倒数第三小
    for i in range(10):
        if (i != di and i != ci ):
            d = oneNumArray[i]
    for i in range(10):
        if(oneNumArray[i][2] != b[2]):
            if(oneNumArray[i][2] > c[2] and oneNumArray[i][2] < d[2]):
                d = oneNumArray[i]

    return [b,c,d] # [[0, 63, 71985.0],[1, 22, 81985.0]]

def getPhoneNumber(url):
    start = time.time()
    # path = str(sys.argv[2])+"test/1.tif"
    imagePath = str(sys.argv[2])+"test/1.gif"
    data = urllib.urlopen(url).read()
    f = file(imagePath,"wb")
    f.write(data)
    f.close()
    im = Image.open(str(sys.argv[2])+"test/1.gif")
    im.save(str(sys.argv[2])+"test/1.tif")
    imagePath = str(sys.argv[2])+"test/1.tif"
    phoneTempArray = []
    phonePosition = []
    c = []
    while(len(phoneTempArray) < 11):
        end = time.time()
        if(float(end-start) > 2):
            return ""
        bc = oneSureNum(imagePath)
        b = bc[0]
        c = bc[1]
        d = bc[2]
        if(b[1] not in phonePosition):
            phoneTempArray.append(b)
            phonePosition.append(b[1])

    phoneTempArray = sort(phoneTempArray)

    phoneNumber1 = ""
    for i in range(11):
        phoneNumber1 = phoneNumber1 + str(phoneTempArray[i][0])

    phoneNumber2 = ""
    for i in range(11):
        if(abs(phoneTempArray[i][1] - c[1]) < 5):
            phoneTempArray[i][0] = c[0]
    for i in range(11):
        phoneNumber2 = phoneNumber2 + str(phoneTempArray[i][0])

    phoneNumber3 = ""
    for i in range(11):
        if(abs(phoneTempArray[i][1] - d[1]) < 5):
            phoneTempArray[i][0] = d[0]
    for i in range(11):
        phoneNumber3 = phoneNumber3 + str(phoneTempArray[i][0])
    os.remove(str(sys.argv[2])+"test/1.tif")
    return phoneNumber1 + "," + phoneNumber2

print getPhoneNumber(str(sys.argv[1]))
# print getPhoneNumber(str(r"http://image.58.com/showphone.aspx?t=v55&v=32DBF4D30F92576ERB77C93F9CB1F6907"))
