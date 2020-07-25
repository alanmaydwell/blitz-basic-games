;Monster

;go into graphics mode
Graphics 640,480
Global width=GraphicsWidth()
Global height=GraphicsHeight()
Global midx=width/2
Global midy=height/2

;enable double buffering
SetBuffer BackBuffer()

txt$="Beware! Beware! Monster word attack ! They're comming to get you ! Look out for the twead!"

Type monst
Field x
Field y
Field oldx
Field oldy
Field dx
Field dy	
Field c$	;Stores single character extracted from string
Field cp	;Character position from text
Field final	;flag for final character in text
End Type

For mult=1 To 6
;Creates text "monster" at given coordinates and initial velocities (text, x-coord, y-oord, x-velocity, y-velocity)
create_monster(txt$,0,0,8,mult)
Next 

;Main Loop. Repeats unless ESC hit.
While Not KeyDown(1)
Cls
update_monsters()
Flip 
Wend
End


;**********************************************************
; Functions start here 
;**********************************************************


Function create_monster(A$,x,y,dx,dy)
length=Len(A$)
For loop=1 To length 
abc.monst=New monst
abc\x=x
abc\y=y
abc\dx=dx
abc\dy=dy
abc\c$=Mid$(A$,loop,1)
abc\cp=loop
If loop =length Then 
abc\final=True
Else abc\final=False
End If 
Next
End Function 


;Updates and plots the "monsters"
Function update_monsters()
For abc.monst =Each monst

Color 255-abc\cp*6,255-abc\cp*6,250
Text abc\x,abc\y,abc\c$

abc\oldx=abc\x
abc\oldy=abc\y

If abc\cp=1 Then
;updates the lead character
abc\x=abc\x+abc\dx
abc\y=abc\y+abc\dy
abc\dx=confine(abc\x,abc\dx,0,width)
abc\dy=confine(abc\y,abc\dy,0,height)

;updates the following characters, each of which takes the previous character's case
Else  
abc=Before abc; moves pointer forward to read the previous character's old coordinates
tempx=abc\oldx
tempy=abc\oldy
abc=After abc; restores pointer to current position
abc\x=tempx
abc\y=tempy
End If 
Next 
End Function

;Stops x# from exceeding min and max values by ensuring direction of velocity is towards the boundary if object leaves the edge.
;Used to stop things leaving the edge of the screen.
Function confine#(x#,dx#,min,max)
If x#<min And dx#<0 Then dx#=-dx#
If x#>max And dx#>0 Then dx#=-dx#
Return dx#
End Function