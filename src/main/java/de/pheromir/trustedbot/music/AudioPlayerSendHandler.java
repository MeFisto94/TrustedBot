/*******************************************************************************
 * Copyright (C) 2019 Pheromir
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package de.pheromir.trustedbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.dv8tion.jda.core.audio.AudioSendHandler;

/**
 * This is a wrapper around AudioPlayer which makes it behave as an
 * AudioSendHandler for JDA. As JDA calls canProvide before every call to
 * provide20MsAudio(), we pull the frame in canProvide() and use the frame we
 * already pulled in provide20MsAudio().
 */
public class AudioPlayerSendHandler implements AudioSendHandler {

	private final AudioPlayer audioPlayer;
	private AudioFrame lastFrame;

	/**
	 * @param audioPlayer
	 *            Audio player to wrap.
	 */
	public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
		this.audioPlayer = audioPlayer;
	}

	@Override
	public boolean canProvide() {
		if (lastFrame == null) {
			lastFrame = audioPlayer.provide();
		}

		return lastFrame != null;
	}

	@Override
	public byte[] provide20MsAudio() {
		if (lastFrame == null) {
			lastFrame = audioPlayer.provide();
		}

		byte[] data = lastFrame != null ? lastFrame.getData() : null;
		lastFrame = null;

		return data;
	}

	@Override
	public boolean isOpus() {
		return true;
	}
}
