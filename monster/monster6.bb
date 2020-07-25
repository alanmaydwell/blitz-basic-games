;Monster

;go into graphics mode
Graphics 640,480

;Set some scaling parameters
Global width=GraphicsWidth()
Global height=GraphicsHeight()
Global midx=width/2
Global midy=height/2

;enable double buffering
SetBuffer BackBuffer()

;Define data type for monsters
Type monst
Field x
Field y
Field oldx
Field oldy
Field dx
Field dy	
Field c$	;Stores single character extracted from string
Field cp	;Character position from original string
Field final	;flag for final character in text
Field mt	;sets the movement behaviour of text
End Type

spawn_monster(2,2,2,2)


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


Function spawn_monster(m1,m2,m3,m4)

For loop=0 To m1 
If loop<>0 Then create_monster("Minator",0,0,8,1+loop,1)
Next

For loop=0 To m2 
If loop<>0 Then create_monster("Centipede-and-another-Centipede",0,loop*8,8,0,2)
Next 

For loop=0 To m3 
If loop<>0 Then create_monster("Harpy",loop*8,0,8,0,3)
Next 

For loop=0 To m4
If loop<>0 Then create_monster("Wisp",0,loop*11,2,2,4)
Next

End Function 

Function create_monster(A$,x,y,dx,dy,mt)
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
abc\mt=mt
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

;updates the lead character
If abc\cp=1 Then

;Applies differnt types of movement for different types of monsters (\mt)
Select abc\mt
Case 1;default (simple reflection)

Case 2; centipede 
If abc\dy=8 Then abc\dy=0
If abc\dy=-8 And abc\y<(height-100) Then abc\dy=0
If (abc\x=0 Or abc\x=width) Then abc\dy=8
If abc\y=height Then abc\dy=-8

Case 3;bouncer
If abc\dy<20 Then abc\dy=abc\dy+1

Case 4;wobbler
x=Rnd(1,5)
If x=1 Then 
abc\dx=abc\dx+Rnd(-1,1)
abc\dy=abc\dy+Rnd(-1,1)
If Abs(abc\dx)>10 Then abc\dx=abc\dx*.8
If Abs(abc\dy)>10 Then abc\dy=abc\dy*.8
End If
 
End Select


;updates the lead character position and confines to screen
abc\dx=confine(abc\x,abc\dx,0,width)
abc\dy=confine(abc\y,abc\dy,0,height)
abc\x=abc\x+abc\dx
abc\y=abc\y+abc\dy

;updates the following characters, each of which takes the preceding character's coordinates
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