;Zap

;go into graphics mode
Graphics 640,480
Global width=GraphicsWidth()
Global height=GraphicsHeight()
Global midx=width/2
Global midy=height/2

;Misc settings
Global rots=32 ;number of orientations for spinning things
Dim trig#(rots,1)

;Game Settings
Global level=1 
Global score=0

;Create paths
Global max=720
Dim path(max,7)

For loop=0 To max
y=loop*width/max
path(loop,0)=y
path(loop,1)=width-y
path(loop,2)=48*Sin(90+loop*2)+(.8*midy)
path(loop,3)=48*Cos(90+loop*2)+(.8*midy)
path(loop,4)=150*Sin(90+loop)+(.8*midy)
path(loop,5)=150*Cos(90+loop)+(.8*midy)
path(loop,6)=150*Sin(90+loop/2)+(.8*midy)
path(loop,7)=150*Cos(90+loop/2)+(.8*midy)
Next 


;Load Graphics
;Tells Blitz Basic  to handle centring of images. (Important for rotation of images)
AutoMidHandle True 
Global im_sig=LoadImage("data\sig2.bmp")
Global im_pris=LoadImage("data\dsotm2.bmp")

Dim im_sigr(rots) ;stores images in different orientations
Dim im_prisr(rots);ditto

;Data type for baddie
Type blob
Field x	 ;x coord
Field xo ;x off set (used for formations)
Field y	 ;y coord
Field yo ;y off set
Field dx ;x velocity
Field dy ;y velocity
Field o  ;orientation
Field p	 ;position in path
Field dp ;movement rate through path
Field im ;image type
Field mt ;movement type	
End Type


;enable double buffering
SetBuffer BackBuffer()

rotate()

Repeat 
set_level(level)


;loop until ESC hit...
While Not KeyDown(1)
Cls
update_blob()
Flip
Wend
Delay 400
clearup()
level=level+1
Until KeyDown(1)
End 


;#######################################################################
;Functions Begin Here
;#######################################################################


;*********************************
;*** Game Management Functions ***
;*********************************

;Sets up each level
Function set_level(level)
Select level 

Case 1
For loop=0 To 6
z=loop*60
spawn(path(0,0),path(0,1),0,0,z,1,1)
spawn(path(0,0),path(0,1),0,50,z,2,1)
spawn(path(0,0),path(0,1),0,0,z,1,2)
spawn(path(0,0),path(0,1),0,50,z,2,2)
Next 

Case 2
For loop=0 To 3
z=loop*60
spawn(path(0,0),path(0,0),z,0,1,1,2)
spawn(path(0,0),path(0,0),0,50,z,2,2)
Next 


Default
For loop=0 To 3
z=loop*42
spawn(path(0,0),path(0,1),0,z,1,1,3)
spawn(path(0,0),path(0,1),100,z,1,2,3)
spawn(path(0,0),path(0,1),200,z,1,1,3)
Next 

End Select
End Function

;Deletes all baddies
Function clearup()
For bem.blob = Each blob
Delete bem
Next
End Function 



;************************
;*** Baddie Functions ***
;************************

;Creates baddie
Function spawn(x,y,xo,yo,p,im,mt)
bem.blob=New blob
bem\x=x
bem\xo=xo
bem\y=y
bem\yo=yo
bem\dx=0
bem\dy=0
bem\o=Rnd(rots) 
bem\p=p
bem\dp=4
bem\mt=mt
bem\im=im
End Function 

;updates baddies
Function update_blob()
For bem.blob = Each blob

Select bem\mt 
Case 1
bem\x=bem\xo+path(bem\p,0)
bem\y=bem\yo+path(bem\p,3)

Case 2 
bem\x=bem\xo+path(bem\p,1)
bem\y=bem\yo+path(bem\p,3)

Case 3
bem\x=bem\xo+path(bem\p,3)
bem\y=bem\yo+path(bem\p,2)

Default
bem\x=bem\xo+path(bem\p,0)
bem\y=bem\yo+path(bem\p,0)


End Select 

If bem\im=1 Then DrawImage im_sigr(bem\o),bem\x,bem\y
If bem\im=2 Then DrawImage im_prisr(bem\o),bem\x,bem\y

bem\p=bem\p+bem\dp 
If bem\p>max Then bem\p=0;max:bem\dp=-bem\dp
If bem\p<0 Then bem\p=0:bem\dp=-bem\dp
bem\o=(bem\o+1) Mod(rots)
Next
End Function 

;generates rotated image
; To get centerimg right the statement"AutoMidHandle True" must appear in program before image loaded.
Function rotate()
For loop=0 To rots
angle=loop*360/rots
im_sigr(loop)=CopyImage(im_sig)
RotateImage im_sigr(loop),angle
im_prisr(loop)=CopyImage(im_pris)
RotateImage im_prisr(loop),angle
trig#(loop,0)=Sin(angle)
trig#(loop,1)=Cos(angle)
If KeyDown(1) End 
Next
End Function 