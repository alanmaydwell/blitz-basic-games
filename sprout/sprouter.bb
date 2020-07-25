;The Sprouter Limits

rots=16 ;number of orientations for ship
Dim frames(rots)
ship=LoadImage("ship.bmp")


;Rotates the image and stores separate frames in frames(loop)
Print"Calculating ..."
For loop=0 To rots-1
frames(loop)=CopyImage(temppic)
RotateImage frames(loop),loop*360/rots
Print loop+" / " + (rots-1)
Next

;go into graphics mode
Graphics 640,480

;enable double buffering
SetBuffer BackBuffer()

;Displays each image in turn to give a rotating image
;loop until ESC hit...
While Not KeyDown(1)
Cls
Text 320,10,"Spin Drier Photography",True,True 
DrawImage(frames(p),300,230)
;Delay 5
p=p+1
If p>rots-1 
p=0
End If
VWait; Wait VBL  
Flip 
Wend
End

 