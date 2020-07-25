;Bones

;go into graphics mode
Graphics 640,480
Global width=GraphicsWidth()
Global height=GraphicsHeight()
Global midx=width/2
Global midy=height/2


;************************
;*** Laser Parameters ***
;************************
;
Global nbeams=0    ;Number of beams in existence
Global maxbeams=8  ;Maximum number of symaltaneous beams allowed
Dim lascol(5,2)    ;Sort of colour palette for laser
Global c=0         ;Colour index for laser beam (position of 1st coordinate in array)

;Stores primary and secondary rgb colour values in array 
Restore lcolourdata
For loop1=0 To 5
For loop2=0 To 2
Read num 
lascol(loop1,loop2)=num 
Next 
Next 

Type laser
Field x
Field y  ;Start point of beam
Field y2 ;end point of beam
Field ymax; maximum end point of beam
End Type 


;enable double buffering
SetBuffer BackBuffer()


;loop until ESC hit...
While Not KeyDown(1)
Cls 
Color 255,255,255
Plot MouseX(),MouseY()
If MouseHit(1) And nbeams<maxbeams Then shoot_laser(MouseX(),MouseY(),480)
update_laser()
Flip 
Wend
End 

;***********************
;*** Laser Functions ***
;***********************

Function shoot_laser(x,y,l)
nbeams=nbeams+1
pop.laser=New laser
pop\x=x
pop\y=y
pop\y2=y
pop\ymax=pop\y-l
End Function

Function update_laser()
For pop.laser = Each laser
Color lascol(c,0),lascol(c,1),lascol(c,2)
Line pop\x,pop\y,pop\x,pop\y2

;Adds speckles to beam
Color 0,0,0
For loop=0 To 10
y=Rnd(pop\y,pop\y2)
Line  pop\x,y,pop\x,y+4
;Plot pop\x,y
Next

;Lengthens beam if it has not reached full extension otherwise shortens it
If pop\y2>=pop\ymax Then 
pop\y2=pop\y2-6
pop\y=pop\y-1
Else pop\y=pop\y-12
End If 

;Deletes beam once length back to zero. Also shifts the beam colour
If pop\y<=pop\ymax Then 
Delete pop
nbeams=nbeams-1
c=(c+1)Mod 5 
End If 

Next
End Function

;rgb Colour Palette data for laser beam
.lcolourdata
Data 255,0,100
Data 255,255,0
Data 0,255,0
Data 0,255,255
Data 100,100,255
Data 255,0,255