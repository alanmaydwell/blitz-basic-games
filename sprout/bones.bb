;Bones

;go into graphics mode
Graphics 640,480

;enable double buffering
SetBuffer BackBuffer()

pic1=LoadImage("camera.bmp")


;loop until ESC hit...
While Not KeyDown(1)
Flip
DrawImage pic1,100,300

Wend

 