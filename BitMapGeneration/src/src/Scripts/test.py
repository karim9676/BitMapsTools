from PIL import Image, ImageDraw, ImageFont

# Define image size
img = Image.new('RGB', (100, 100), 'white')
draw = ImageDraw.Draw(img)

# Load a Unicode font (ensure it's installed)
font = ImageFont.truetype("../fonts/Arial.ttf", 40)

# Draw a similar Unicode symbol
draw.text((10, 30), "⇄", font=font, fill="black")

# Save or show the image
img.save("icon_bitmap.png")
img.show()

