//-----------------------------------------------------------------------------
// luna2d sharing service for Twitter
// This is part of luna2d engine
// Copyright 2014-2017 Stepan Prokofjev
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.
//-----------------------------------------------------------------------------

#import "TwitterSharing.h"
#import <Social/Social.h>

@implementation TwitterSharing

-(NSString*) getName
{
	return @"twitter";
}

// Share given text
-(void) text: (NSString*)text
{
	UIViewController* rootViewController = [[[[UIApplication sharedApplication] delegate] window] rootViewController];
	
	SLComposeViewController* twitterController = [SLComposeViewController composeViewControllerForServiceType:SLServiceTypeTwitter];
	[twitterController setInitialText:text];
	[rootViewController presentViewController:twitterController animated:YES completion:nil];
}

// Share given image with given text
// Image should be located in "LUNAFileLocation::APP_FOLDER"
-(void) image: (NSString*)filename text: (NSString*)text
{
	UIViewController* rootViewController = [[[[UIApplication sharedApplication] delegate] window] rootViewController];
	UIImage* image = [UIImage imageNamed:filename];
	
	SLComposeViewController* twitterController = [SLComposeViewController composeViewControllerForServiceType:SLServiceTypeTwitter];
	[twitterController setInitialText:text];
	[twitterController addImage:image];
	[rootViewController presentViewController:twitterController animated:YES completion:nil];
}

@end
