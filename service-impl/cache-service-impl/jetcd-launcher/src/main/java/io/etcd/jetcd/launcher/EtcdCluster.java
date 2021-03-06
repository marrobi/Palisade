/**
 * Copyright 2017 The jetcd authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etcd.jetcd.launcher;

import javax.annotation.Nonnull;

import java.net.URI;
import java.util.List;

/**
 * This class has been copied out of the jetcd github repo (https://github.com/etcd-io/jetcd)
 *
 * This class should be removed once jetcd release a version which contains the jetcd-launcher package
 */
public interface EtcdCluster extends AutoCloseable {

  void start();

  @Override
  void close();

  @Nonnull
  List<URI> getClientEndpoints();

  @Nonnull
  List<URI> getPeerEndpoints();
}
