import os, struct, zlib
W,H=500,270
px=[[(7,4,6,255) for _ in range(W)] for _ in range(H)]
def rect(x0,y0,x1,y1,c):
    for y in range(max(0,y0),min(H,y1)):
        row=px[y]
        for x in range(max(0,x0),min(W,x1)): row[x]=c
def line(x0,y0,x1,y1,c,w=1):
    if y0==y1: rect(min(x0,x1),y0,max(x0,x1)+1,y0+w,c)
    elif x0==x1: rect(x0,min(y0,y1),x0+w,max(y0,y1)+1,c)
def outline(x,y,w,h,c,t=1):
    rect(x,y,x+w,y+t,c);rect(x,y+h-t,x+w,y+h,c);rect(x,y,x+t,y+h,c);rect(x+w-t,y,x+w,y+h,c)
def diamond(cx,cy,r,c):
    for yy in range(-r,r+1):
        span=r-abs(yy); rect(cx-span,cy+yy,cx+span+1,cy+yy+1,c)
def ornament(cx,cy,flip=1):
    dark=(54,8,15,255); red=(132,13,28,255); hot=(238,28,48,255)
    diamond(cx,cy,8,dark);diamond(cx,cy,5,red);diamond(cx,cy,2,hot)
    for d in range(1,5):
        x=cx+flip*(9+d*5); line(cx+flip*7,cy,x,cy-d*2,red); diamond(x,cy-d*2,2,dark)
# deep layered frame
rect(0,0,W,H,(4,2,3,255)); outline(0,0,W,H,(40,5,10,255),3); outline(4,4,W-8,H-8,(166,14,30,255),2); outline(8,8,W-16,H-16,(70,9,17,255),1)
# metallic rails
for x in range(12,W-12,18):
    rect(x,5,x+12,9,(24,18,20,255)); line(x,6,x+10,6,(91,22,29,255))
for x in range(12,W-12,18): rect(x,H-9,x+12,H-5,(24,18,20,255))
for y in range(18,H-18,18): rect(5,y,9,y+12,(24,18,20,255)); rect(W-9,y,W-5,y+12,(24,18,20,255))
# corner armor
for cx,cy in [(12,12),(W-13,12),(12,H-13),(W-13,H-13)]:
    diamond(cx,cy,10,(31,12,16,255)); diamond(cx,cy,6,(100,11,23,255)); diamond(cx,cy,2,(244,34,52,255))
# header plaque
rect(135,8,365,39,(18,7,10,255)); outline(135,8,230,31,(89,11,22,255),2); outline(144,13,212,21,(157,18,33,255),1)
# pointed plaque ends
for i in range(18):
    rect(135-i,14+i//2,136-i,33-i//2,(92,10,20,255)); rect(364+i,14+i//2,365+i,33-i//2,(92,10,20,255))
# top open-book silhouette / crimson pedestal
rect(232,0,268,8,(25,11,14,255)); diamond(250,6,9,(95,10,22,255)); diamond(250,6,4,(239,30,50,255))
# left item panel
rect(14,44,119,250,(11,6,8,255)); outline(14,44,105,206,(92,12,23,255),2)
rect(27,55,106,126,(18,8,12,255)); outline(27,55,79,71,(125,14,29,255),1)
ornament(17,48,1); ornament(116,48,-1)
# category strip and list well
rect(126,44,481,77,(10,6,8,255)); outline(126,44,355,33,(65,13,20,255),1)
rect(126,80,481,249,(8,5,7,255)); outline(126,80,355,169,(75,11,20,255),1)
# six inset row plates
for i in range(6):
    y=82+i*28; rect(130,y,474,y+24,(22,9,13,255)); outline(130,y,344,24,(76,15,24,255),1)
    # right upgrade button bed
    rect(401,y+3,467,y+21,(65,10,17,255)); outline(401,y+3,66,18,(174,18,34,255),1)
# skill rail bottom
rect(126,250,481,267,(9,5,7,255)); line(139,258,391,258,(75,25,32,255),2)
# central gothic accents
ornament(250,42,1); ornament(250,H-8,1)
# subtle red glow pixels around frame
for y in range(12,H-12,9):
    if y%18==0: rect(10,y,12,y+2,(122,11,25,255));rect(W-12,y,W-10,y+2,(122,11,25,255))

def png(path):
    raw=b''.join(b'\x00'+bytes(sum(([r,g,b,a] for r,g,b,a in row),[])) for row in px)
    def chunk(t,d): return struct.pack('>I',len(d))+t+d+struct.pack('>I',zlib.crc32(t+d)&0xffffffff)
    data=b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',W,H,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw,9))+chunk(b'IEND',b'')
    os.makedirs(os.path.dirname(path),exist_ok=True);open(path,'wb').write(data)
png('project/src/client/resources/assets/enchantment_progression/textures/gui/gothic_progression.png')
